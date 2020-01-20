package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDoc;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeValue;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeValue_;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute_;
import com.ozguryazilim.raf.entities.ExternalDoc_;
import com.ozguryazilim.telve.data.RepositoryBase;
import java.util.List;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
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

    public List<ExternalDocTypeAttributeValue> search(List<ExternalDocTypeAttributeValue> externalDocTypeAttributeValues) {
        Criteria<ExternalDocTypeAttributeValue, ExternalDocTypeAttributeValue> crt = criteria()
                .select(ExternalDocTypeAttributeValue.class);

        for (ExternalDocTypeAttributeValue val : externalDocTypeAttributeValues) {
            if (val != null) {
                crt = crt.join(ExternalDocTypeAttributeValue_.externalDocTypeAttribute,
                        where(ExternalDocTypeAttribute.class)
                                .eq(ExternalDocTypeAttribute_.attributeName, val.getExternalDocTypeAttribute().getAttributeName())
                ).like(ExternalDocTypeAttributeValue_.value, "%".concat(val.getValue()).concat("%"));
            }
        }

        return crt.getResultList();
    }
}
