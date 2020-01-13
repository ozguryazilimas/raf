package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeValue;
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
public abstract class ExternalDocTypeAttributeValueRepository extends RepositoryBase<ExternalDocTypeAttributeValue, ExternalDocTypeAttributeValue> implements CriteriaSupport<ExternalDocTypeAttributeValue> {

    public abstract List<ExternalDocTypeAttributeValue> findByDocumentTypeAndExternalDocTypeAttribute(ExternalDocType documentDocType, ExternalDocTypeAttribute externalDocTypeAttribute);

    public abstract List<ExternalDocTypeAttributeValue> findByDocumentTypeAndExternalDocTypeAttributeAndRafFilePath(ExternalDocType documentDocType, ExternalDocTypeAttribute externalDocTypeAttribute, String rafFilePath);

    public abstract List<ExternalDocTypeAttributeValue> findByEternalDocTypeAttribute(ExternalDocTypeAttribute externalDocTypeAttribute);

    public abstract List<ExternalDocTypeAttributeValue> findByDocumentType(ExternalDocType documentDocType);

    public abstract void removeByEternalDocTypeAttribute(ExternalDocTypeAttribute externalDocTypeAttribute);

    public abstract List<ExternalDocTypeAttributeValue> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);
}
