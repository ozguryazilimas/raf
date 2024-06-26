package com.ozguryazilim.raf.member;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafMember;
import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.telve.data.RepositoryBase;
import java.util.List;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 *
 * @author oyas
 */
@Repository
@Dependent
public abstract class RafMemberRepository extends RepositoryBase<RafMember, RafMember> implements CriteriaSupport<RafMember>{

    public abstract List<RafMember> findByMemberName(String memberName);

    public abstract List<RafMember> findByRaf( RafDefinition rd );
    
    public abstract List<RafMember> findByRafAndMemberNameAndMemberType( RafDefinition rd, String memberName, RafMemberType memberType );
    
    public abstract void removeByRaf( RafDefinition rd );
}
