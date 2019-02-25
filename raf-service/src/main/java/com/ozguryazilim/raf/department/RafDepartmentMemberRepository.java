package com.ozguryazilim.raf.department;

import com.ozguryazilim.raf.entities.RafDepartment;
import com.ozguryazilim.raf.entities.RafDepartmentMember;
import com.ozguryazilim.telve.data.RepositoryBase;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import javax.enterprise.context.Dependent;
import java.util.List;

@Repository
@Dependent
public abstract class RafDepartmentMemberRepository extends RepositoryBase<RafDepartmentMember, RafDepartmentMember> implements CriteriaSupport<RafDepartmentMember> {

    abstract List<RafDepartmentMember> findByDepartment(RafDepartment department);

    abstract List<RafDepartmentMember> findByMemberName(String memberName);
}
