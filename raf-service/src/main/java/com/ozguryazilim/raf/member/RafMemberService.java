package com.ozguryazilim.raf.member;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafUserRoleService;
import com.ozguryazilim.raf.ReadOnlyModeService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.telve.audit.AuditLogCommand;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserLookup;
import com.ozguryazilim.telve.idm.IdmEvent;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.ozguryazilim.telve.idm.ldapSync.IdmLdapSyncEvent;
import com.ozguryazilim.telve.idm.user.UserGroupRepository;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
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
import java.util.stream.Stream;

import javax.enterprise.event.Observes;

/**
 * Raf üyeliklerini yönetmek için servis sınıfı.
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafMemberService implements Serializable {
    private static final String eventLogTokenSeperator = "$%&";

    public static final String RAF_ROLE_MANAGER = "MANAGER";

    private static final Logger LOG = LoggerFactory.getLogger(RafMemberService.class);

    private Map<RafDefinition, List<RafMember>> memberMap = new HashMap<>();
    private Map<String, List<String>> groupUsers = new HashMap<>();

    @Inject
    private RafMemberRepository memberRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private UserGroupRepository userGrouprepository;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

   @Inject
    private UserLookup userLookup;

    @Inject
    private ReadOnlyModeService readOnlyModeService;

    @Inject
    private RafDefinitionService rafDefinitionService;

    public List<RafMember> getMembers(RafDefinition raf) throws RafException {
        if (hasManagerRole(identity.getLoginName(), raf)) {
            return getMembersImpl(raf);
        }
        sendAuditLog(raf.getNodeId(), "UNAUTHORIZED_QUERY", String.format("Raf Name: %s, Member: %s", raf.getCode(), identity.getLoginName()));
        throw new RafException("[RAF-0042] Permission Denied");
    }

    public void addMember(RafDefinition raf, String member, RafMemberType type, String role) throws RafException {
        RafMember m = new RafMember();
        m.setMemberName(member);
        m.setMemberType(type);
        m.setRaf(raf);
        m.setRole(role);
        addMember(m);
    }

    public void addMembers(RafDefinition raf, List<String> members, RafMemberType type, String role) throws RafException {
        for (String m : members) {
            addMember(raf, m, type, role);
        }
    }

    private String generateEventLogMessage(RafMember member, String eventType) {
        StringJoiner sj = new StringJoiner(eventLogTokenSeperator);
        String memberName = (RafMemberType.USER.equals(member.getMemberType()) ? userLookup.getUserName(member.getMemberName()) : member.getMemberName());
        return sj.add("event." + eventType)
                .add(memberName)
                .add(member.getRaf().getCode())
                .add(member.getRole())
                .add(identity.getUserName())
                .toString();

    }

    @Transactional
    public void addMember(RafMember member) throws RafException {
        if (!isMemberOf(member.getMemberName(), member.getRaf(), false)) {
            memberRepository.saveAndFlush(member);
            //Cache'e de koyalım
            getMembersImpl(member.getRaf()).add(member);

            String eventType = "RafMemberServiceAddMember" + (RafMemberType.GROUP.equals(member.getMemberType()) ? ".group" : ".user");

            if (Boolean.parseBoolean(ConfigResolver.getPropertyValue("auditLog.raf.addMember", "false"))) {
                sendAuditLog(member.getRaf().getNodeId(), "ADD_MEMBER", String.format("Raf Name: %s, Member: %s, Member Type: %s, Member Role: %s", member.getRaf().getCode(), member.getMemberName(), member.getMemberType(), member.getRole()));
            }
            commandSender.sendCommand(EventLogCommandBuilder.forRaf(member.getRaf().getCode())
                    .eventType(eventType)
                    .path("/RAF/" + member.getRaf().getCode() + "/")
                    .message(generateEventLogMessage(member, eventType))
                    .user(identity.getLoginName())
                    .build());
        }        
    }

    @Transactional
    public void removeMember(RafMember member) throws RafException {
        memberRepository.remove(member);
        //Cache'den de çıkaralım
        getMembersImpl(member.getRaf()).remove(member);

        String eventType = "RafMemberServiceRemoveMember" + (RafMemberType.GROUP.equals(member.getMemberType()) ? ".group" : ".user");

        if (Boolean.parseBoolean(ConfigResolver.getPropertyValue("auditLog.raf.removeMember", "false"))) {
            sendAuditLog(member.getRaf().getNodeId(), "REMOVE_MEMBER", String.format("Raf Name: %s, Member: %s, Member Type: %s, Member Role: %s", member.getRaf().getCode(), member.getMemberName(), member.getMemberType(), member.getRole()));
        }
        commandSender.sendCommand(EventLogCommandBuilder.forRaf(member.getRaf().getCode())
                .eventType(eventType)
                .path("/RAF/" + member.getRaf().getCode() + "/")
                .message(generateEventLogMessage(member, eventType))
                .user(identity.getLoginName())
                .build());
    }

    @Transactional
    public void changeMemberRole(RafMember member) throws RafException {
        memberRepository.saveAndFlush(member);
        sendAuditLog(member.getRaf().getNodeId(), "CHANGE_ROLE", String.format("Raf Name: %s, Member: %s, New Role: %s", member.getRaf().getCode(), member.getMemberName(), member.getRole()));
    }

    /**
     * Kullanıcı Raf'a üye mi?
     *
     * @param username
     * @param raf
     * @return
     * @throws RafException
     */
    public boolean isMemberOf(String username, RafDefinition raf) throws RafException {
        return isMemberOf(username, raf, true);
    }

    /**
     * Kullanıcı ya da grup raf'a üye mi?
     *
     * @param memberName
     * @param raf
     * @param groupsIncluded true verilir ise grup üyelikleri de kontrol edilir
     * @return
     * @throws RafException
     */
    public boolean isMemberOf(String memberName, RafDefinition raf, boolean groupsIncluded) throws RafException {

        //PRIVATE ve SHARED repolarda manager yok ama geri kalan bütün kullanıcılar tam yetkili.
        if (raf.getCode().equals("PRIVATE") || (raf.getCode().equals("SHARED") && identity.hasPermission("sharedRaf", "select"))) {
            //PRIVATE için aslında kullanıcı kontrolü yapılabilir ama anlamlı değil çünkü PATH her zaman kullanıcı adı içerir.
            return true;
        }

        //Kullanıcı ya da grup fark etmez üye mi diye bakıyoruz.
        boolean b = getMembersImpl(raf).stream()
                .anyMatch(m -> m.getMemberName().equals(memberName));

        if (!b && groupsIncluded) {
            //Burada üye gruplar üzerinden bir kontrol yapalım.
            //Önce Grup tipi memberlar alınıp bunlardan üye listesi toplanıyor
            //Ardından o liste içinde istenilen kullanıcı var mı diye bakılıyor.
            b = getMembersImpl(raf).stream()
                    .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                    .flatMap(m -> getGroupUsers(m.getMemberName()).stream())
                    .anyMatch(s -> s.equals(memberName));
        }

        return b;
    }

    public boolean hasManagerRole(String username, RafDefinition raf) throws RafException {
        if (username == null || raf == null) {
            return false;
        }
        return hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasReadRole(String username, RafDefinition raf) throws RafException {
        if (username == null || raf == null) {
            return false;
        }
        return hasMemberRole(username, "CONSUMER", raf) || hasMemberRole(username, "CONTRIBUTER", raf) || hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "SUPPORTER", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasWriteRole(String username, RafDefinition raf) throws RafException {
        if (readOnlyModeService.isEnabled()) {
            return false;
        }

        if (username == null || raf == null) {
            return false;
        }
        return hasMemberRole(username, "CONTRIBUTER", raf) || hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf) || hasMemberRole(username, "SUPPORTER", raf);
    }

    public boolean hasCheckoutRole(String username, RafDefinition raf) throws RafException {
        if (readOnlyModeService.isEnabled()) {
            return false;
        }

        if (username == null || raf == null) {
            return false;
        }
        return hasMemberRole(username, "CONTRIBUTER", raf) || hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasDeleteRole(String username, RafDefinition raf) throws RafException {
        if (readOnlyModeService.isEnabled()) {
            LOG.debug("User: {} Raf: {}. Read-only mode is active. Return delete role permission: false", username, raf.getCode());
            return false;
        }

        if(username == null) {
            LOG.debug("User is null. Return delete role permission: false");
            return false;
        }
        if(raf == null) {
            LOG.debug("User : {}. Raf is null. Return delete role permission: false", username);
            return false;
        }
        return hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    private boolean hasMemberRole(String username, String role, RafDefinition raf) throws RafException {
        if (username == null || raf == null) {
            return false;
        }
        //PRIVATE ve SHARED repolarda manager yok ama geri kalan bütün kullanıcılar tam yetkili.
        if (raf.getCode().equals("PRIVATE") || raf.getCode().equals("SHARED")) {
            Boolean permission = !"MANAGER".equals(role);
            return permission;
        }

        Stream<RafMember> rafMemberStream = getMembersImpl(raf).stream()
                .filter(m -> m.getMemberType().equals(RafMemberType.USER) && m.getMemberName().equals(username));
        Stream<RafMember> rafGroupStream = getMembersImpl(raf).stream()
                .filter(m -> m.getMemberType().equals(RafMemberType.GROUP) && getGroupUsers(m.getMemberName()).contains(username));

        Boolean permission =  Stream.concat(rafMemberStream, rafGroupStream)
                .anyMatch(m -> m.getRole().equals(role));
        LOG.debug("User: {}, Role: {}. Checked raf members and group members. Return permission: {}", username, role, permission);
        return permission;
    }

    public boolean hasMemberAnyRole(String username, Set<String> roles, RafDefinition raf) throws RafException {
        //PRIVATE ve SHARED repolarda manager yok ama geri kalan bütün kullanıcılar tam yetkili.
        if (raf.getCode().equals("PRIVATE") || raf.getCode().equals("SHARED")) {
            return roles.stream().anyMatch(role -> !role.equals("MANAGER"));
        }
        return roles.contains(getMemberRole(username, raf));
    }

    public String getMemberRole(String username, String rafCode) throws RafException {
        return getMemberRole(username, rafDefinitionService.getRafDefinitionByCode(rafCode));
    }

    public String getMemberRole(String username, RafDefinition raf) {
        Stream<RafMember> rafMemberStream = getMembersImpl(raf).stream()
                .filter(m -> m.getMemberType().equals(RafMemberType.USER) && m.getMemberName().equals(username));
        Stream<RafMember> rafGroupStream = getMembersImpl(raf).stream()
                .filter(m -> m.getMemberType().equals(RafMemberType.GROUP) && getGroupUsers(m.getMemberName()).contains(username));

        return Stream.concat(rafMemberStream, rafGroupStream)
                .map(RafMember::getRole)
                .max(RafUserRoleService.roleComparator)
                .orElse("");
    }

    /**
     * Asıl implementasyon. Cache'e bakar. Yoksa veri tabanından toparlar.
     *
     * @param raf
     * @return
     * @throws RafException
     */
    protected List<RafMember> getMembersImpl(RafDefinition raf) {

        List<RafMember> r = memberMap.get(raf);

        if (r == null) {
            r = memberRepository.findByRaf(raf);
            memberMap.put(raf, r);
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
     * Gruplar üzerinde bir değişiklik oldugunda cache'leri temizliyoruz
     *
     * @param event
     */
    public void onIdmEvent(@Observes IdmEvent event) {
        if (event.getFrom().equals(IdmEvent.FROM_GROUP)) {
            this.groupUsers.clear();
        }
    }

    /**
     * LDAP grupları üzerinde sync yapıldığında cache'leri temizliyoruz.
     *
     * @param event
     */
    public void onIdmLdapSyncEvent(@Observes IdmLdapSyncEvent event) {
        if (event.getSyncType().equals(IdmLdapSyncEvent.GROUP)) {
            this.groupUsers.clear();
        }
    }

    private void sendAuditLog(String id, String action, String message) {
        if (identity != null && !"SYSTEM".equals(identity.getLoginName())) {
            //create audit log
            AuditLogCommand auditCommand = new AuditLogCommand("RAF", Long.MIN_VALUE, id, action, "RAF", identity.getLoginName(), message);
            commandSender.sendCommand(auditCommand);
        }
    }

}
