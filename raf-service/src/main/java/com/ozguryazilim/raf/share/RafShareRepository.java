package com.ozguryazilim.raf.share;

import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.telve.data.RepositoryBase;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import javax.enterprise.context.Dependent;
import java.util.List;

@Repository
@Dependent
public abstract class RafShareRepository extends RepositoryBase<RafShare, RafShare> implements CriteriaSupport<RafShare> {

    public abstract List<RafShare> findByNodeId(String nodeId);

    public abstract void deleteByNodeId(String nodeId);

    public abstract void deleteByToken(String token);

    public abstract List<RafShare> findByTokenAndPassword(String token, String password);

}