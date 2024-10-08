package com.ozguryazilim.raf.objet.member;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.ReadOnlyModeService;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.entities.RafPathMember;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.utils.UrlUtils;
import com.ozguryazilim.telve.audit.AuditLogCommand;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserLookup;
import com.ozguryazilim.telve.auth.UserService;
import com.ozguryazilim.telve.idm.IdmEvent;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.ozguryazilim.telve.idm.user.UserGroupRepository;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Raf nesne üyeliklerini yönetmek için servis sınıfı.
 *
 * FIXME: auditLog eklenecek.
 *
 * FIXME: grup değişikliklerinde grup map'i boşaltılmalı
 *
 * @author oyas
 */
@ApplicationScoped
public class RafPathMemberService implements Serializable {
    private static final String eventLogTokenSeperator = "$%&";

    private static final Logger LOG = LoggerFactory.getLogger(RafPathMemberService.class);

    private Map<String, List<RafPathMember>> memberMap = new HashMap<>();
    private Map<String, List<String>> groupUsers = new HashMap<>();

    @Inject
    private RafPathMemberRepository memberRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private UserGroupRepository userGrouprepository;

    @Inject
    private UserService userService;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

    @Inject
    private UserLookup userLookup;

    @Inject
    private ReadOnlyModeService readOnlyModeService;

    public List<RafPathMember> getMembers(String path) throws RafException {
        //FIXME: Yetki kontrolü. Bu sorguyu çekenin bunu yapmaya yetkisi var mı?
        return getMembersImpl(path);
    }

    public void addMember(String path, String member, RafMemberType type, String role) throws RafException {
        if (Boolean.parseBoolean(ConfigResolver.getPropertyValue("auditLog.raf.addMember", "false"))) {
            sendMemberAuditLog("ADD_MEMBER", path, member, type.name(), role);
        }

        RafPathMember m = new RafPathMember();
        m.setMemberName(member);
        m.setMemberType(type);
        m.setPath(path);
        m.setRole(role);
        addMember(m);
    }

    public void addMembers(String path, List<String> members, RafMemberType type, String role) throws RafException {
        for (String m : members) {
            addMember(path, m, type, role);
        }
    }

    private String generateEventLogMessage(RafPathMember member, String eventType) {
        StringJoiner sj = new StringJoiner(eventLogTokenSeperator);
        String memberName = (RafMemberType.USER.equals(member.getMemberType()) ? userLookup.getUserName(member.getMemberName()) : member.getMemberName());
        return sj.add("event." + eventType)
                .add(memberName)
                .add(member.getPath().replace("/RAF/", ""))
                .add(member.getRole())
                .add(identity.getUserName())
                .toString();

    }

    @Transactional
    public void addMember(RafPathMember member) throws RafException {
        if (!isMemberOf(member.getMemberName(), member.getPath(), false)) {
            memberRepository.saveAndFlush(member);
            //Cache'e de koyalım
            getMembersImpl(member.getPath()).add(member);

            String eventType = "RafMemberServiceAddMember" + (RafMemberType.GROUP.equals(member.getMemberType()) ? ".group" : ".user");

            if (Boolean.parseBoolean(ConfigResolver.getPropertyValue("auditLog.raf.addMember", "false"))) {
                sendMemberAuditLog("ADD_MEMBER", member.getPath(), member.getMemberName(), member.getMemberName(), member.getRole());
            }
            commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                    .eventType(eventType)
                    .path(member.getPath())
                    .message(generateEventLogMessage(member, eventType))
                    .user(identity.getLoginName())
                    .build());
        }
    }

    @Transactional
    public void removeMember(String path, String username, RafMemberType memberType) throws RafException {
        //FIXME: bu methoda ihtiyaç var mı?
        //memberRepository.remove(entity);
    }

    @Transactional
    public void removeMember(RafPathMember member) throws RafException {
        memberRepository.remove(member);
        //Cache'den de çıkaralım
        getMembersImpl(member.getPath()).remove(member);

        String eventType = "RafMemberServiceRemoveMember" + (RafMemberType.GROUP.equals(member.getMemberType()) ? ".group" : ".user");

        if (Boolean.parseBoolean(ConfigResolver.getPropertyValue("auditLog.raf.removeMember", "false"))) {
            sendMemberAuditLog("REMOVE_MEMBER", member.getPath(), member.getMemberName(), member.getMemberName(), member.getRole());
        }
        commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                    .eventType(eventType)
                    .path(member.getPath())
                    .message(generateEventLogMessage(member, eventType))
                    .user(identity.getLoginName())
                    .build());
    }

    public void changeMemberRole(String path, String username, String role) throws RafException {

    }

    /**
     * Kullanıcı Raf'a üye mi?
     *
     * @param username
     * @param path
     * @return
     * @throws RafException
     */
    public boolean isMemberOf(String username, String path) throws RafException {
        return isMemberOf(username, path, true);
    }

    /**
     * Kullanıcı ya da grup raf'a üye mi?
     *
     * @param memberName
     * @param path
     * @param groupsIncluded true verilir ise grup üyelikleri de kontrol edilir
     * @return
     * @throws RafException
     */
    public boolean isMemberOf(String memberName, String path, boolean groupsIncluded) throws RafException {

        //Kullanıcı ya da grup fark etmez üye mi diye bakıyoruz.
        boolean b = getMembersImpl(path).stream()
                .anyMatch(m -> m.getMemberName().equals(memberName));

        if (!b && groupsIncluded) {
            //Burada üye gruplar üzerinden bir kontrol yapalım.
            //Önce Grup tipi memberlar alınıp bunlardan üye listesi toplanıyor
            //Ardından o liste içinde istenilen kullanıcı var mı diye bakılıyor.
            b = getMembersImpl(path).stream()
                    .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                    .flatMap(m -> getGroupUsers(m.getMemberName()).stream())
                    .anyMatch(s -> s.equals(memberName));
        }

        return b;
    }

    public boolean hasManagerRole(String username, String path) throws RafException {
        return hasMemberRole(username, "MANAGER", path);
    }

    public boolean hasReadRole(String username, String path) throws RafException {
        return hasMemberRole(username, "CONSUMER", path) || hasMemberRole(username, "CONTRIBUTER", path) || hasMemberRole(username, "EDITOR", path) || hasMemberRole(username, "MANAGER", path);
    }

    public boolean hasWriteRole(String username, String path) throws RafException {
        if (readOnlyModeService.isEnabled()) {
            return false;
        }

        return hasMemberRole(username, "CONTRIBUTER", path) || hasMemberRole(username, "EDITOR", path) || hasMemberRole(username, "MANAGER", path) || hasMemberRole(username, "SUPPORTER", path);
    }

    public boolean hasDeleteRole(String username, String path) throws RafException {
        if (readOnlyModeService.isEnabled()) {
            return false;
        }

        return hasMemberRole(username, "EDITOR", path) || hasMemberRole(username, "MANAGER", path);
    }

    public boolean hasCheckoutRole(String username, String path) throws RafException {
        if (readOnlyModeService.isEnabled()) {
            return false;
        }

        return hasMemberRole(username, "CONTRIBUTER", path) || hasMemberRole(username, "EDITOR", path) || hasMemberRole(username, "MANAGER", path);
    }

    private boolean hasMemberRole(String username, String role, String path) throws RafException {
        if (isMemberOf(username, path)) {
            LOG.debug("User: {} Role: {} Path: {} Checking member role...", username, role, path);
            return hasMemberRole_(username, role, path);
        } else {
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            if (Strings.isNullOrEmpty(parentPath)) {
                LOG.debug("User: {} Role: {} Path: {} Parent path is empty. Return permission: false", username, role, path);
                return false;
            } else {
                LOG.debug("User: {} Role: {} Path: {} Trying to check member role recursively from parent path.", username, role, parentPath);
                return hasMemberRole(username, role, parentPath);
            }
        }
    }

    public boolean hasMemberAnyRole(String username, Set<String> roles, String path) throws RafException {
        if (isMemberOf(username, path)) {
            LOG.debug("User: {} Roles: {} Path: {} Trying to check member roles...", username, roles, path);
            boolean hasUserRole = getMembersImpl(path).stream()
                    .anyMatch(m -> m.getMemberName().equals(username) && roles.contains(m.getRole()));

            if (!hasUserRole) {
                LOG.debug("User: {} Roles: {} Path: {} Haven't user role. Trying to check group member roles", username, roles, path);
                //Dahil olduğu gruptan (varsa) ilgili rolü alıp almadığını kontrol edelim
                Boolean result =  getMembersImpl(path).stream()
                        .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                        .filter(m -> getGroupUsers(m.getMemberName()).stream().anyMatch(username::equals))
                        .anyMatch(m -> roles.contains(m.getRole()));
                LOG.debug("User: {} Roles: {} Path: {} Group member roles checked completed. Return permission: {}", username, roles, path, result);
                return result;
            }
            LOG.debug("User: {} Roles: {} Path: {} Member roles checked completed. Return permission: {}", username, roles, path, hasUserRole);
            return hasUserRole;
        } else {
            LOG.debug("User: {} Roles: {} Path: {} Cannot found membership to the path. Trying to parent path recursively.", username, roles, path);
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            if (Strings.isNullOrEmpty(parentPath)) {
                LOG.debug("User: {} Roles: {} Path: {} Parent path is empty. Return permission: false", username, roles, path);
                return false;
            } else {
                return hasMemberAnyRole(username, roles, parentPath);
            }
        }
    }

    public boolean hasMemberInPath(String username, String path) {
        try {
            LOG.debug("User: {} Path: {} Trying to check only memberships...", username, path);
            if (isMemberOf(username, path)) {
                LOG.debug("User: {} Path: {} User is the member of path. Return permission: true", username, path);
                return true;
            } else {
                LOG.debug("User: {} Path: {} Cannot found membership to the path. Trying to parent path recursively.", username, path);
                String parentPath = path.substring(0, path.lastIndexOf("/"));
                if (Strings.isNullOrEmpty(parentPath)) {
                    LOG.debug("User: {} Path: {} Parent path is empty. Return permission: false", username, path);
                    return false;
                } else {
                    return hasMemberInPath(username, parentPath);
                }
            }
        } catch (RafException ex) {
            LOG.error("HasMemberInPath method throw an exception: {}", ex);
            return false;
        }
    }

    private boolean hasMemberRole_(String username, String role, String path) throws RafException {

        boolean b = getMembersImpl(path).stream()
                .anyMatch(m -> m.getMemberName().equals(username) && m.getRole().equals(role));
        LOG.debug("User: {} Role: {} Path: {} Member role: {}", username, role, path, b);

        //Kullanıcı olarak tanımlı olmayıp, grup üzerinden rolü olabilir.
        if (!b) {
            //Önce kullanıcının dahil olduğu grubu bulalım
            List<RafPathMember> grps = getMembersImpl(path).stream()
                    .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                    .collect(Collectors.toList());
            LOG.debug("User: {} Role: {} Path: {} Groups: {}", username, role, path, grps);

            //Şimdi her grup içine bakalım kullanıcı var mı?
            for (RafPathMember m : grps) {
                if (getGroupUsers(m.getMemberName()).stream().anyMatch(s -> s.equals(username))) {
                    //Varsa role doğru mu?
                    if (m.getRole().equals(role)) {
                        LOG.debug("User: {} Role: {} Path: {} Group role is matched. Return permission: true", username, role, path);
                        return true;
                    }
                }

            }
        }
        LOG.debug("User: {} Role: {} Path: {} Return permission : {}", username, role, path, b);
        return b;
    }

    public String getMemberRole(String username, String path) {
        //Kullanıcı ya da grup fark etmez üye mi diye bakıyoruz.
        List<RafPathMember> b = getMembersImpl(path).stream()
                .filter(m -> m.getMemberName().equals(username))
                .collect(Collectors.toList());

        if (b == null || b.isEmpty()) {
            //Burada üye gruplar üzerinden bir kontrol yapalım.
            //Önce Grup tipi memberlar alınıp bunlardan üye listesi toplanıyor
            //Ardından o liste içinde istenilen kullanıcı var mı diye bakılıyor.
            b = getMembersImpl(path).stream()
                    .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                    .filter(m -> getGroupUsers(m.getMemberName()).contains(username))
                    .collect(Collectors.toList());
        }
        return b != null && !b.isEmpty() ? b.get(0).getRole() : "";
    }


    /**
     * Asıl implementasyon. Cache'e bakar. Yoksa veri tabanından toparlar.
     *
     * @param path
     * @return
     * @throws RafException
     */
    protected List<RafPathMember> getMembersImpl(String path) {

        List<RafPathMember> r = memberMap.get(path);

        if (r == null) {
            r = memberRepository.findByPath(path);
            memberMap.put(path, r);
        }

        return r;
    }

    public List<String> getGroupUsers(String userGroupName) {

        List<String> r = groupUsers.get(userGroupName);

        if (r == null) {
            r = getGroupUsersImpl(userGroupName);
            groupUsers.put(userGroupName, r);
        }

        return r;
    }

    protected List<String> getGroupUsersImpl(String userGroupName) {
        List<Group> ls = groupRepository.findByCode(userGroupName);
        if (ls.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (Group g : ls) {
            result.addAll(
                    userGrouprepository.findByGroup(g).stream()
                            .map(gu -> gu.getUser().getLoginName())
                            .collect(Collectors.toList())
            );
        }

        return result;
    }

    /**
     * Grup üzerinde değişklik olduğunda ilgili grubun veritabanından güncellenmesi için grubu cache den temizliyoruz.
     *
     * @param event
     */
    public void onIdmEvent(@Observes IdmEvent event) {
        if (IdmEvent.FROM_GROUP.equals(event.getFrom()) && IdmEvent.UPDATE.equals(event.getAction())) {
            groupUsers.put(event.getSubject(), null);
        }
    }

    private void sendAuditLog(String id, String action, String message) {
        if (identity != null && !"SYSTEM".equals(identity.getLoginName())) {
            //create audit log
            AuditLogCommand auditCommand = new AuditLogCommand("RAF", Long.MIN_VALUE, id, action, "RAF", identity.getLoginName(), message);
            commandSender.sendCommand(auditCommand);
        }
    }

    private void sendMemberAuditLog(String action, String path, String memberName, String memberType, String memberRole) {
        String memberLogText = String.format("Member: %s, Member Type: %s, Member Role: %s", memberName, memberType, memberRole);
        String pathNameLogTextTemplate = "Path Name: %s";
        int pathTextSize = 255 - (memberLogText.length() + pathNameLogTextTemplate.length()) - 5;
        sendAuditLog("", action, String.format(pathNameLogTextTemplate + memberLogText, UrlUtils.trimRafPaths(path, pathTextSize, pathTextSize / 2)));
    }
}
