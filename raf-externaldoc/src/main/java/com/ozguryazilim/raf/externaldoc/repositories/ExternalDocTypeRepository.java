package com.ozguryazilim.raf.externaldoc.repositories;

import com.ozguryazilim.raf.externaldoc.entities.ExternalDocType;
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
public abstract class ExternalDocTypeRepository extends RepositoryBase<ExternalDocType, ExternalDocType> implements CriteriaSupport<ExternalDocType> {

    public abstract List<ExternalDocType> findByDocumentType(String documentType);

    public abstract void removeByDocumentType(String documentType);

    public void removeAll() {
        entityManager().createNativeQuery("delete from external_doc_type").executeUpdate();
    }
}
