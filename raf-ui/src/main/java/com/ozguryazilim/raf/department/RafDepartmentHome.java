package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
import com.ozguryazilim.raf.entities.RafDepartmentMember;
import com.ozguryazilim.telve.data.TreeRepositoryBase;
import com.ozguryazilim.telve.forms.ParamEdit;
import com.ozguryazilim.telve.forms.TreeBase;
import javax.inject.Inject;


@ParamEdit
public class RafDepartmentHome extends TreeBase<RafDepartment> {

    @Inject
    private RafDepartmentRepository departmentRepository;

    @Inject
    private RafDepartmentService departmentService;

    private RafDepartmentMember selectedMember;
    
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

    public RafDepartmentMember getSelectedMember() {
        return selectedMember;
    }

    public void setSelectedMember(RafDepartmentMember selectedMember) {
        this.selectedMember = selectedMember;
    }

    public void addNewMember(){
        this.selectedMember = new RafDepartmentMember();
        //selectedMember.setDepartment(getEntity());
    }
    
    public void editMember( int ix ){
        this.selectedMember = getEntity().getMembers().get(ix);
    }
    
    public void saveMember(){
        if( selectedMember.getDepartment() == null ){
            selectedMember.setDepartment(getEntity());
            getEntity().getMembers().add(selectedMember);
        }
     
    }
    
    public void removeMember(int ix) {
        getEntity().getMembers().remove(ix);
    }
}
