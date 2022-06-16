package com.ozguryazilim.raf.share;

import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.entities.RafShare_;
import com.ozguryazilim.telve.data.RepositoryBase;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import javax.enterprise.context.Dependent;
import java.util.Date;
import java.util.List;

@Repository
@Dependent
public abstract class RafShareRepository extends RepositoryBase<RafShare, RafShare> implements CriteriaSupport<RafShare> {

    public abstract List<RafShare> findByNodeId(String nodeId);

    public abstract List<RafShare> findByShareGroup(String shareGroup);

    public abstract void deleteByShareGroup(String shareGroup);

    public abstract void deleteByNodeId(String nodeId);

    public abstract void deleteByToken(String token);

    public abstract List<RafShare> findByTokenAndPassword(String token, String password);

    public abstract List<RafShare> findBySharedBy(String shareBy);

    public List<RafShare> findActiveSharesBySharedBy(String sharedBy) {
        Criteria<RafShare, RafShare> crit = criteria();

        crit.eq(RafShare_.sharedBy, sharedBy);
        crit.or(
            criteria().gtOrEq(RafShare_.endDate, new Date()),
            criteria().eq(RafShare_.endDate, null)
        );
        crit.orderDesc(RafShare_.id);

        return crit.createQuery().getResultList();
    }
}