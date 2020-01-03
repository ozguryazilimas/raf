package com.ozguryazilim.raf.objet.member;

import com.ozguryazilim.raf.entities.RafMemberType;
import com.ozguryazilim.raf.entities.RafPathMember;
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
public abstract class RafPathMemberRepository extends RepositoryBase<RafPathMember, RafPathMember> implements CriteriaSupport<RafPathMember> {

    public abstract List<RafPathMember> findByPath(String path);

    public abstract List<RafPathMember> findByPathAndMemberNameAndMemberType(String path, String memberName, RafMemberType memberType);

    public abstract void removeByPath(String path);
}
