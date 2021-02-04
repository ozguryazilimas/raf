package com.ozguryazilim.raf.externaldoc.repositories;

import com.ozguryazilim.raf.externaldoc.entities.ExternalDocType;
import com.ozguryazilim.raf.externaldoc.entities.ExternalDocTypeAttribute;
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
public abstract class ExternalDocTypeAttributeRepository extends RepositoryBase<ExternalDocTypeAttribute, ExternalDocTypeAttribute> implements CriteriaSupport<ExternalDocTypeAttribute> {

    public abstract List<ExternalDocTypeAttribute> findByDocumentTypeAndAttributeName(ExternalDocType documentDocType, String attributeName);

    public abstract List<ExternalDocTypeAttribute> findByAttributeName(String attributeName);

    public abstract List<ExternalDocTypeAttribute> findByDocumentType(ExternalDocType documentDocType);

    public abstract void removeByAttributeName(String attributeName);

    public abstract void removeByDocumentType(ExternalDocType documentDocType);

    public void removeAll() {
        entityManager().createNativeQuery("delete from external_doc_type_attribute").executeUpdate();
    }
}
