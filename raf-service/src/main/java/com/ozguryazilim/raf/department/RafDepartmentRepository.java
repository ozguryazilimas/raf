package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
import com.ozguryazilim.telve.data.TreeRepositoryBase;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

@Repository
@Dependent
public abstract class RafDepartmentRepository extends TreeRepositoryBase<RafDepartment> implements CriteriaSupport<RafDepartment> {

    public RafDepartment newRafDepartment(RafDepartment parent) {
        RafDepartment entity = new RafDepartment();
        entity.setParent(parent);
        return entity;
    }
}
