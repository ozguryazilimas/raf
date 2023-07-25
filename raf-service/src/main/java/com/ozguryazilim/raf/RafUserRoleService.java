package com.ozguryazilim.raf;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.entities.RafPathMember;
import com.ozguryazilim.raf.member.RafMemberRepository;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.objet.member.RafPathMemberRepository;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserService;
import org.jodconverter.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestScoped
public class RafUserRoleService implements Serializable {

    private final Logger LOG = LoggerFactory.getLogger(RafUserRoleService.class);

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private RafMemberRepository memberRepository;

    @Inject
    private RafPathMemberRepository rafPathMemberRepository;

    @Inject
    private Identity identity;

    @Inject
    private UserService userService;

    private Map<String, List<RafMember>> rafRoleMap = new HashMap<>();
    private Map<String, List<RafPathMember>> rafPathRoleMap = new HashMap<>();

    private List<RafMember> getRafMembersByLoginName(String loginName) {
        List<RafMember> r = rafRoleMap.get(loginName);

        if (r == null) {
            r = memberRepository.findByMemberName(loginName);
            rafRoleMap.put(loginName, r);
        }

        return r;
    }

    private List<RafPathMember> getRafPathRolesByLoginName(String loginName) {
        List<RafPathMember> r = rafPathRoleMap.get(loginName);

        if (r == null) {
            r = rafPathMemberRepository.findByMemberName(loginName);
            rafPathRoleMap.put(loginName, r);
        }

        return r;
    }

    private String getRafMemberRoleByLoginName(String loginName, String rafCode) {
        //Kullanıcı ya da grup fark etmez üye mi diye bakıyoruz.
        List<RafMember> b = new ArrayList<>(getRafMembersByLoginName(loginName));

        if (b == null || b.isEmpty()) {
            //Burada üye gruplar üzerinden bir kontrol yapalım.
            //Önce Grup tipi memberlar alınıp bunlardan üye listesi toplanıyor
            //Ardından o liste içinde istenilen kullanıcı var mı diye bakılıyor.
            b = getRafMembersByLoginName(loginName).stream()
                    .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                    .filter(m -> rafMemberService.getGroupUsers(m.getMemberName()).contains(loginName))
                    .collect(Collectors.toList());
        }
        return b.stream()
                .filter(rafMember -> rafMember.getRaf().getCode().equals(rafCode))
                .map(RafMember::getRole)
                .findAny()
                .orElse("");
    }

    public Map<String, String> getRafRolesOfCurrentUser() {
        return getRafMembersByLoginName(identity.getLoginName()).stream()
                .collect(Collectors.toMap(
                        (RafMember rafMember) -> rafMember.getRaf().getCode(),
                        (RafMember rafMember) -> getRafMemberRoleByLoginName(identity.getLoginName(), rafMember.getRaf().getCode())
                ));
    }

    public Map<String, String> getPathRolesByLoginName(String loginName) {
        return getRafPathRolesByLoginName(loginName).stream()
                .collect(Collectors.toMap(
                        RafPathMember::getPath,
                        (RafPathMember pathMember) -> rafPathMemberService.getMemberRole(loginName, pathMember.getPath())
                ));
    }

    public Map<String, String> getPathRolesOfCurrentUserByRaf(String rafName) {
        return getPathRolesByLoginName(identity.getLoginName()).entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("/RAF/" + rafName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public String getRoleInPath(String loginName, String path) {

        if (RafPathUtils.isInGeneralRaf(path)) {
            String pathRole = rafPathMemberService.getMemberRole(loginName, path);

            if (StringUtils.isBlank(pathRole)) {
                String parentPath = path.substring(0, path.lastIndexOf("/"));

                try {
                    if (RafPathUtils.isRafRootPath(parentPath)) {
                        return rafMemberService.getMemberRole(identity.getLoginName(), RafPathUtils.getRafCodeByPath(path));
                    } else {
                        return getRoleInPath(loginName, parentPath);
                    }
                } catch (RafException e) {
                    LOG.error(String.format("Error while getting user role. Path: %s", path));
                    return "";
                }

            } else {
                return pathRole;
            }
        } else {
            LOG.error(String.format("Error while getting user role. Path is not belonging to /RAF/. Path: %s", path));
            return "";
        }
    }


    public List<UserInfo> getUsersWithRoleFromPath(String path, String... roles) throws RafException {
        String rafCode = RafPathUtils.getRafCodeByPath(path);

        List<String> roleList = Arrays.asList(roles);

        List<RafMember> rafMembers = rafMemberService.getMembers(rafDefinitionService.getRafDefinitionByCode(rafCode));
        List<String> groupUsers = rafMembers.stream()
                .filter(user -> user.getMemberType().equals(RafMemberType.GROUP))
                .flatMap(user -> rafMemberService.getGroupUsers(user.getMemberName()).stream())
                .collect(Collectors.toList());

        List<String> uniqueMemberNames = Stream.concat(
                    rafMembers.stream()
                            .filter(user -> user.getMemberType().equals(RafMemberType.USER))
                            .map(RafMember::getMemberName),
                    groupUsers.stream()
                )
                .distinct()
                .collect(Collectors.toList());


        return uniqueMemberNames.stream()
                .filter(memberName -> roleList.contains(getRoleInPath(memberName, path)))
                .map(memberName -> userService.getUserInfo(memberName))
                .collect(Collectors.toList());
    }

}
