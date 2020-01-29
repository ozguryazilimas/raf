package com.ozguryazilim.raf.externaldoc;

import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
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
}
