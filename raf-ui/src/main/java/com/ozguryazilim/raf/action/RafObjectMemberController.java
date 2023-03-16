package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.entities.RafPathMember;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.auth.UserInfo;
import com.ozguryazilim.telve.auth.UserLookup;
import com.ozguryazilim.telve.auth.UserService;
import com.ozguryazilim.telve.idm.entities.Group;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class RafObjectMemberController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafObjectMemberController.class);

    @Inject
    private Identity identity;

    @Inject
    private RafService rafService;

    @Inject
    private RafPathMemberService memberService;

    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;

    @Inject
    private UserLookup userLookup;

    @Inject
    private UserService userService;

    private RafObject rafObject;
    private String path;

    private List<UserInfo> selectedUsers = new ArrayList<>();
    private String role;

    private Group userGroup;

    public void init() {

        try {
            if (!Strings.isNullOrEmpty(path)) {
                rafObject = rafService.getRafObjectByPath(path);
            }
        } catch (RafException ex) {
            //FIXME: Burada ne yapmalı?
            LOG.error("Error", ex);
        }

        selectedUsers.clear();
        //FIXME: burda default rol ataması yapılabilir belki?
        role = "";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public RafObject getRafObject() {
        return rafObject;
    }

    public void setRafObject(RafObject rafObject) {
        this.rafObject = rafObject;
    }

    public List<RafPathMember> getMembers() {
        //FIXME: burada ikide bir sorgu çekmenin anlamı yok. Cacheleyelim.
        try {
            return memberService.getMembers(path);
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Raf Exception", ex);
            FacesMessages.error("Üye bilgisi alınamadı", ex.getLocalizedMessage());
        }

        return Collections.emptyList();
    }

    public List<UserInfo> completeUser(String query) {

        LOG.info("Seçili Kullanıcılar : {}}", selectedUsers);

        //FIXME: localeSelector den mi alsak? Yoksa Telve tarafına butür şeyler için bir locale mi tanımlatsak?
        Locale locale = new Locale("tr");

        //FIXME: daha önce seçilmiş olanları da filtreleyelim.

        return getUsers().stream()
                .filter(u -> u.getUserName().toUpperCase(locale).contains(query.toUpperCase(locale)) || u.getLoginName().contains(query))
                .collect(Collectors.toList());
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
            memberService.addMembers(path, members, RafMemberType.USER, role);
            selectedUsers.clear();
            userGroup = null;
            role = "";
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
            memberService.addMember(path, userGroup.getCode(), RafMemberType.GROUP, role);
            selectedUsers.clear();
            userGroup = null;
            role = "";
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Memeber Add Error", ex);
            FacesMessages.error("Grup eklenemedi", ex.getLocalizedMessage());
        }

    }

    public List<String> getGroupUsers(String userGroupName) {
        return memberService.getGroupUsers(userGroupName);
    }

    public void deleteMember(RafPathMember member) {
        try {
            memberService.removeMember(member);
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Member cannot delete", ex);
            FacesMessages.error("Kullanıcı Silinemedi", ex.getLocalizedMessage());
        }
    }

    private List<UserInfo> getUsers() {
        return userService.getLoginNames().stream()
                .map(userService::getUserInfo)
                .collect(Collectors.toList());
    }
}
