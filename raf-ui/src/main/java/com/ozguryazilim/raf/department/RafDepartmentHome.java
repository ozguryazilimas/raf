package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
import com.ozguryazilim.raf.entities.RafDepartmentMember;
import com.ozguryazilim.telve.data.TreeRepositoryBase;
import com.ozguryazilim.telve.forms.ParamEdit;
import com.ozguryazilim.telve.forms.TreeBase;
import com.ozguryazilim.telve.idm.entities.User;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.primefaces.event.SelectEvent;

@ParamEdit
public class RafDepartmentHome extends TreeBase<RafDepartment> {

    @Inject
    private RafDepartmentRepository departmentRepository;

    @Inject
    private RafDepartmentService departmentService;

    @Inject
    private RafDepartmentMemberRepository memberRepository;

    protected RafDepartment getParent() {
        RafDepartment parent = null;
        if (getTreeModel().getSelectedData() != null) {
            parent = getTreeModel().getSelectedData();
        }
        return parent;
    }

    public void newRafDepartment() {
        setEntity(departmentRepository.newRafDepartment(getParent()));
    }

    public void newRootRafDepartment() {
        setEntity(departmentRepository.newRafDepartment(null));
    }

    @Override
    protected boolean onBeforeSave() {
        getEntity().setCode(getEntity().getName());
        return true;
    }

    @Override
    protected boolean onAfterSave() {
        departmentService.refresh();
        return super.onAfterSave();
    }

    @Override
    protected void onAfterDelete() {
        departmentService.refresh();
        super.onAfterDelete();
    }

    @Override
    protected TreeRepositoryBase<RafDepartment> getRepository() {
        return departmentRepository;
    }

    public List<RafDepartmentMember> getMembers() {
        if (!getEntity().isPersisted()) {
            return Collections.emptyList();
        }
        return memberRepository.findByDepartment(getEntity());

    }

    public void onMemberSelect(SelectEvent event) {
        List<User> users = getUsers(event);
        addMembers(users);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<User> getUsers(SelectEvent event) {
        LookupSelectTuple tuple = (LookupSelectTuple) event.getObject();

        List<User> ls = new ArrayList<>();
        if (tuple != null) {
            if (tuple.getValue() instanceof List) {
                ls.addAll((List<User>) tuple.getValue());
            } else {
                ls.add((User) tuple.getValue());
            }
        }

        return ls;
    }

    protected void addMembers(List<User> users) {

        RafDepartment department = getEntity();
        for (User u : users) {
            if (!isDepartmentMember(u.getLoginName(), department)) {
                getMember(u, department);
            }
        }
        departmentService.saveDepartment(department);
        search();
    }

    protected boolean isDepartmentMember(String loginName, RafDepartment department) {
        Optional<RafDepartmentMember> member = memberRepository.findByMemberName(loginName);
        if (member.isPresent() && department.getMembers() != null && department.getMembers().contains(member.get())) {
            return true;
        } else return false;
    }

    @Transactional
    public void removeMember(Long id) {
        RafDepartmentMember member = memberRepository.findBy(id);
        memberRepository.remove(member);
    }

    @Transactional
    private RafDepartmentMember getMember(User user, RafDepartment department) {
        Optional<RafDepartmentMember> memberOptional = memberRepository.findByMemberName(user.getLoginName());
        if (memberOptional.isPresent()) {
            return memberOptional.get();
        } else {
            RafDepartmentMember member = new RafDepartmentMember();
            member.setDepartment(department);
            member.setMemberName(user.getLoginName());
            memberRepository.save(member);
            return member;
        }
    }


}
