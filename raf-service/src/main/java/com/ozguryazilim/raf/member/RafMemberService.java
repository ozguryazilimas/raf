package com.ozguryazilim.raf.member;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.telve.audit.AuditLogCommand;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.idm.IdmEvent;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.ozguryazilim.telve.idm.ldapSync.IdmLdapSyncEvent;
import com.ozguryazilim.telve.idm.user.UserGroupRepository;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
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
import java.util.stream.Collectors;

/**
 * Raf üyeliklerini yönetmek için servis sınıfı.
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafMemberService implements Serializable {

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

    public List<RafMember> getMembers(RafDefinition raf) throws RafException {
        String role = getMemberRole(identity.getLoginName(), raf);
        if (role.equals(RAF_ROLE_MANAGER)) {
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

    @Transactional
    public void addMember(RafMember member) throws RafException {
        if (!isMemberOf(member.getMemberName(), member.getRaf(), false)) {
            memberRepository.saveAndFlush(member);
            //Cache'e de koyalım
            getMembersImpl(member.getRaf()).add(member);

            commandSender.sendCommand(EventLogCommandBuilder.forRaf(member.getRaf().getCode())
                    .eventType("RafMemberServiceAddMember")
                    .message("event.RafMemberServiceAddMember$%&" + member.getMemberName() + "$%&" + member.getRaf().getCode() + "$%&" + member.getRole() + "$%&" + identity.getUserName())
                    .user(identity.getLoginName())
                    .build());
        }
    }

    @Transactional
    public void removeMember(RafMember member) throws RafException {
        memberRepository.remove(member);
        //Cache'den de çıkaralım
        getMembersImpl(member.getRaf()).remove(member);
        commandSender.sendCommand(EventLogCommandBuilder.forRaf(member.getRaf().getCode())
                .eventType("RafMemberServiceRemoveMember")
                .message("event.RafMemberServiceRemoveMember$%&" + member.getMemberName() + "$%&" + member.getRaf().getCode() + "$%&" + member.getRole() + "$%&" + identity.getUserName())
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
        if (raf.getCode().equals("PRIVATE") || raf.getCode().equals("SHARED")) {
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
        return hasMemberRole(username, "CONSUMER", raf) || hasMemberRole(username, "CONTRIBUTER", raf) || hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasWriteRole(String username, RafDefinition raf) throws RafException {
        if (username == null || raf == null) {
            return false;
        }
        return hasMemberRole(username, "CONTRIBUTER", raf) || hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasDeleteRole(String username, RafDefinition raf) throws RafException {
        if (username == null || raf == null) {
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
            return !"MANAGER".equals(role);
        }

        boolean b = getMembersImpl(raf).stream()
                .anyMatch(m -> m.getMemberName().equals(username) && m.getRole().equals(role));

        //Kullanıcı olarak tanımlı olmayıp, grup üzerinden rolü olabilir.
        if (!b) {
            //Önce kullanıcının dahil olduğu grubu bulalım
            List<RafMember> grps = getMembersImpl(raf).stream()
                    .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                    .collect(Collectors.toList());

            //Şimdi her grup içine bakalım kullanıcı var mı?
            for (RafMember m : grps) {
                if (getGroupUsers(m.getMemberName()).stream().anyMatch(s -> s.equals(username))) {
                    //Varsa role doğru mu?
                    if (m.getRole().equals(role)) {
                        return true;
                    }
                }

            }
        }

        return b;
    }

    public String getMemberRole(String username, String rafCode) throws RafException {
        return "";
    }

    public String getMemberRole(String username, RafDefinition raf) throws RafException {
        //Kullanıcı ya da grup fark etmez üye mi diye bakıyoruz.
        List<RafMember> b = getMembersImpl(raf).stream()
                .filter(m -> m.getMemberName().equals(username))
                .collect(Collectors.toList());

        if (b == null || b.isEmpty()) {
            //Burada üye gruplar üzerinden bir kontrol yapalım.
            //Önce Grup tipi memberlar alınıp bunlardan üye listesi toplanıyor
            //Ardından o liste içinde istenilen kullanıcı var mı diye bakılıyor.
            b = getMembersImpl(raf).stream()
                    .filter(m -> m.getMemberType().equals(RafMemberType.GROUP))
                    .filter(m -> getGroupUsers(m.getMemberName()).contains(username))
                    .collect(Collectors.toList());
        }
        return b != null && !b.isEmpty() ? b.get(0).getRole() : "";
    }

    /**
     * Asıl implementasyon. Cache'e bakar. Yoksa veri tabanından toparlar.
     *
     * @param raf
     * @return
     * @throws RafException
     */
    protected List<RafMember> getMembersImpl(RafDefinition raf) throws RafException {

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
