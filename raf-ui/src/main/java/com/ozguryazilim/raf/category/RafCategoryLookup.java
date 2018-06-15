/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.category;

import com.ozguryazilim.raf.config.RafCategoryPages;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.entities.RafCategory_;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.lookup.Lookup;
import com.ozguryazilim.telve.lookup.LookupTreeControllerBase;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@Lookup(dialogPage = RafCategoryPages.RafCategoryLookup.class)
public class RafCategoryLookup extends LookupTreeControllerBase<RafCategory, RafCategory>{

    @Inject
    private RafCategoryRepository repository;
    
    @Override
    protected RepositoryBase<RafCategory, RafCategory> getRepository() {
        return repository;
    }

    @Override
    public String getCaptionFieldName() {
        return RafCategory_.name.getName();
    }
    
}
