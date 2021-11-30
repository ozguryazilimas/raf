package com.ozguryazilim.raf.member;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.config.RafPages;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserLookup;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.view.Pages;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class RafMemberController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafMemberController.class);

    @Inject
    private Identity identity;

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Inject
    private RafDefinitionService definitionService;

    @Inject
    private RafMemberService memberService;

    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;

    @Inject
    private UserLookup userLookup;

    private RafDefinition rafDefinition;
    private String rafCode;

    //Kullanıcı seçimi için userLookup üzerinden toparlanıp cache'lenecek. 
    private List<UserInfo> users;

    private List<UserInfo> selectedUsers = new ArrayList<>();
    private String role;

    private Group userGroup;

    private String filter = "";
    private List<RafMember> filteredMembers;
    private RafMember selectedMember;

    public void init() {

        filteredMembers = null;

        if (Strings.isNullOrEmpty(rafCode)) {
            rafCode = "PRIVATE";
        }

        try {
            rafDefinition = definitionService.getRafDefinitionByCode(rafCode);
        } catch (RafException ex) {
            //FIXME: Burada ne yapmalı?
            LOG.error("Error", ex);
            viewNavigationHandler.navigateTo(Pages.Home.class);
        }

        try {
            //Uye değilse hemen HomePage'e geri gönderelim.
            if (!memberService.isMemberOf(identity.getLoginName(), rafDefinition)) {
                viewNavigationHandler.navigateTo(Pages.Home.class);
            } else if (!memberService.hasManagerRole(identity.getLoginName(), rafDefinition)) {
                navigationParameterContext.addPageParameter("id", rafDefinition.getCode());
                viewNavigationHandler.navigateTo(RafPages.class);
            }
        } catch (RafException ex) {
            LOG.error("Error", ex);
            //Gene de geldiği yere gönderelim.
            viewNavigationHandler.navigateTo(Pages.Home.class);
        }

        selectedUsers.clear();
        //FIXME: burda default rol ataması yapılabilir belki?
        role = "";

    }

    public String getRafCode() {
        return rafCode;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public RafDefinition getRafDefinition() {
        return rafDefinition;
    }

    public void setRafDefinition(RafDefinition rafDefinition) {
        this.rafDefinition = rafDefinition;
    }

    public List<RafMember> getMembers() {

        if (filteredMembers == null) {
            try {
                filteredMembers = memberService.getMembers(rafDefinition).stream()
                        .filter(m
                                -> m.getMemberType().equals(RafMemberType.USER)
                        ? userLookup.getUserName(m.getMemberName()).contains(filter)
                        : m.getMemberName().contains(filter) || getGroupUserNames(m.getMemberName()).stream().filter(gm -> gm.contains(filter)).findAny().isPresent()
                        )
                        .collect(Collectors.toList());

                return filteredMembers;
            } catch (RafException ex) {
                //FIXME: i18n
                LOG.error("Raf Exception", ex);
                FacesMessages.error("Üye bilgisi alınamadı", ex.getLocalizedMessage());
            }

            return Collections.emptyList();
        }

        return filteredMembers;
    }

    public List<UserInfo> completeUser(String query) {

        LOG.info("Seçili Kullanıcılar : {}}", selectedUsers);

        if (users == null) {
            populateUsers();
        }

        //FIXME: localeSelector den mi alsak? Yoksa Telve tarafına butür şeyler için bir locale mi tanımlatsak?
        Locale locale = new Locale("tr");

        //FIXME: daha önce seçilmiş olanları da filtreleyelim.
        List<UserInfo> result = users.stream()
                .filter(u -> u.getUserName().toUpperCase(locale).contains(query.toUpperCase(locale)) || u.getLoginName().contains(query))
                .collect(Collectors.toList());

        return result;
    }

    protected void populateUsers() {

        users = new ArrayList<>();

        userLookup.getLoginNames().forEach((s) -> {
            users.add(userLookup.getUserInfo(s));
        });
    }

    public List<UserInfo> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<UserInfo> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void addSelectedUsers() {
        LOG.info("Kullanıcı Eklenecek : {}}", selectedUsers);

        List<String> members = selectedUsers.stream()
                .map(ui -> ui.getLoginName())
                .collect(Collectors.toList());

        try {
            memberService.addMembers(rafDefinition, members, RafMemberType.USER, role);
            selectedUsers.clear();
            userGroup = null;
            role = "";
            filteredMembers = null;
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Memeber Add Error", ex);
            FacesMessages.error("Kullanıcılar eklenemedi", ex.getLocalizedMessage());
        }

    }

    public Group getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(Group userGroup) {
        this.userGroup = userGroup;
    }

    public void addSelectedGroup() {
        LOG.info("Grup Eklenecek : {}}", userGroup);

        if (userGroup == null) {
            return;
        }

        try {
            memberService.addMember(rafDefinition, userGroup.getCode(), RafMemberType.GROUP, role);
            selectedUsers.clear();
            userGroup = null;
            role = "";
            filteredMembers = null;
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Memeber Add Error", ex);
            FacesMessages.error("Grup eklenemedi", ex.getLocalizedMessage());
        }

    }

    public List<String> getGroupUsers(String userGroupName) {
        return memberService.getGroupUsers(userGroupName);
    }

    public List<String> getGroupUserNames(String userGroupName) {
        return memberService.getGroupUsers(userGroupName).stream().map(m -> userLookup.getUserName(m)).collect(Collectors.toList());
    }

    private boolean isLastManagerMember(RafMember member) {
        if (RafDefinitionService.RAF_ROLE_MANAGER.equals(member.getRole())) {
            List<RafMember> managerMembers = getManagerMembers();
            return managerMembers.isEmpty() || (managerMembers.size() == 1 && managerMembers.get(0) == member);
        }
        return false;
    }

    public void deleteMember(RafMember member) {
        try {
            if (isLastManagerMember(member)) {
                LOG.error("Last manager member cannot delete");
                FacesMessages.error("Son Yönetici Üye Silinemez.", "Yeni bir yönetici üye ekledikten sonra tekrar deneyiniz.");
            } else {
                memberService.removeMember(member);
            }
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Member cannot delete", ex);
            FacesMessages.error("Kullanıcı Silinemedi", ex.getLocalizedMessage());
        }
    }

    public void search() {
        filteredMembers = null;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public RafMember getSelectedMember() {
        return selectedMember;
    }

    public void setSelectedMember(RafMember selectedMember) {
        this.selectedMember = selectedMember;
    }

    public void editMember(RafMember m) {
        selectedMember = m;
    }

    public void closeDialog() {
        //Normal bir kapanış olduğuna göre ya save işlemi yapılacak demek.

        if (selectedMember != null) {

            try {
                memberService.changeMemberRole(selectedMember.getRaf(), selectedMember.getMemberName(), selectedMember.getRole());
            } catch (RafException ex) {
                LOG.error("Role Cannot Change", ex);
            }

            search();
        }

    }

    public void cancelDialog() {
        //Aslında yapacak bir şey yok.
        selectedMember = null;
    }

    public List<RafMember> getManagerMembers() {
        try {
            List<RafMember> result = memberService.getMembers(rafDefinition).stream()
                    .filter(m
                            -> m.getRole().equals(RafDefinitionService.RAF_ROLE_MANAGER))
                    .collect(Collectors.toList());

            return result;
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Raf Exception", ex);
            FacesMessages.error("Yönetici Üye bilgisi alınamadı", ex.getLocalizedMessage());
        }

        return Collections.emptyList();
    }
}
