package com.ozguryazilim.raf.member;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.telve.auth.UserService;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.idm.group.GroupRepository;
import com.ozguryazilim.telve.idm.user.UserGroupRepository;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raf üyeliklerini yönetmek için servis sınıfı.
 *
 * FIXME: auditLog eklenecek.
 *
 * FIXME: grup değişikliklerinde grup map'i boşaltılmalı
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafMemberService implements Serializable {

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
    private UserService userService;

    public List<RafMember> getMembers(RafDefinition raf) throws RafException {
        //FIXME: Yetki kontrolü. Bu sorguyu çekenin bunu yapmaya yetkisi var mı?
        return getMembersImpl(raf);
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
        }
    }

    @Transactional
    public void removeMember(RafDefinition raf, String username, RafMemberType memberType) throws RafException {
        //FIXME: bu methoda ihtiyaç var mı?
        //memberRepository.remove(entity);
    }

    @Transactional
    public void removeMember(RafMember member) throws RafException {
        memberRepository.remove(member);
        //Cache'den de çıkaralım
        getMembersImpl(member.getRaf()).remove(member);
    }

    public void changeMemberRole(String rafCode, String username, String role) throws RafException {

    }

    public void changeMemberRole(RafDefinition raf, String username, String role) throws RafException {

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
        return hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasReadRole(String username, RafDefinition raf) throws RafException {
        return hasMemberRole(username, "CONSUMER", raf) || hasMemberRole(username, "CONTRIBUTER", raf) || hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasWriteRole(String username, RafDefinition raf) throws RafException {
        return hasMemberRole(username, "CONTRIBUTER", raf) || hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    public boolean hasDeleteRole(String username, RafDefinition raf) throws RafException {
        return hasMemberRole(username, "EDITOR", raf) || hasMemberRole(username, "MANAGER", raf);
    }

    private boolean hasMemberRole(String username, String role, RafDefinition raf) throws RafException {

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
        return "";
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
}