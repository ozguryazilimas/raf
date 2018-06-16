/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.category;

import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.telve.data.TreeRepositoryBase;
import com.ozguryazilim.telve.forms.ParamEdit;
import com.ozguryazilim.telve.forms.TreeBase;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ParamEdit
public class RafCategoryHome extends TreeBase<RafCategory>{

    @Inject
    private RafCategoryRepository repository;
    
    @Inject
    private RafCategoryService categoryService;
    
    protected RafCategory getParent() {
        RafCategory parent = null;
        if (getTreeModel().getSelectedData() != null) {
            parent = getTreeModel().getSelectedData();
        }
        return parent;
    }
    
    public void newRafCategory() {
        setEntity(repository.newRafCategory(getParent()));
    }

    public void newRootRafCategory() {
        setEntity(repository.newRafCategory(null));
    }
    
    @Override
    protected boolean onBeforeSave() {
        getEntity().setCode(getEntity().getName());
        return true;
    }

    @Override
    protected boolean onAfterSave() {
        categoryService.refresh();
        return super.onAfterSave();
    }

    @Override
    protected void onAfterDelete() {
        categoryService.refresh();
        super.onAfterDelete();
    }
    
    @Override
    protected TreeRepositoryBase<RafCategory> getRepository() {
        return repository;
    }
    
}
