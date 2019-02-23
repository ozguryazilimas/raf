package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.config.RafDepartmentPages;
import com.ozguryazilim.raf.entities.RafDepartment;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.lookup.Lookup;
import com.ozguryazilim.telve.lookup.LookupTreeControllerBase;
import javax.inject.Inject;

@Lookup(dialogPage = RafDepartmentPages.RafDepartmentLookup.class)
public class RafDepartmentLookup extends LookupTreeControllerBase<RafDepartment, RafDepartment> {

    @Inject
    private RafDepartmentRepository repository;

    @Override
    protected RepositoryBase<RafDepartment, RafDepartment> getRepository() {
        return repository;
    }

    @Override
    public String getCaptionFieldName() {
        return RafDepartment.class.getName();
    }

}
