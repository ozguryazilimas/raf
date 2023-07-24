package com.ozguryazilim.raf;

import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.entities.RafPathMember;
import com.ozguryazilim.raf.member.RafMemberRepository;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.objet.member.RafPathMemberRepository;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestScoped
public class RafUserRoleService implements Serializable {

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private RafMemberRepository memberRepository;

    @Inject
    private RafPathMemberRepository rafPathMemberRepository;

    @Inject
    private Identity identity;

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


}
