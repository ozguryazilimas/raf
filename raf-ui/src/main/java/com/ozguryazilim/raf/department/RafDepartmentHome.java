package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
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

}
