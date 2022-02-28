package com.ozguryazilim.raf.document.comment;

import com.ozguryazilim.raf.entities.RafDocumentComment;
import com.ozguryazilim.raf.entities.RafDocumentComment_;
import com.ozguryazilim.telve.data.RepositoryBase;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import javax.enterprise.context.Dependent;
import java.util.List;

@Repository
@Dependent
public abstract class RafDocumentCommentRepository extends RepositoryBase<RafDocumentComment, RafDocumentComment> implements CriteriaSupport<RafDocumentComment> {

    public List<RafDocumentComment> findAll() {
        return criteria().orderDesc(RafDocumentComment_.date).createQuery().getResultList();
    }

    public List<RafDocumentComment> findByNodeId(String nodeId) {
        return criteria().eq(RafDocumentComment_.nodeId, nodeId).orderDesc(RafDocumentComment_.date).createQuery().getResultList();
    }

}