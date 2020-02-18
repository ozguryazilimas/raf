package com.ozguryazilim.raf.externaldoc;

import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeList;
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
public abstract class ExternalDocTypeAttributeListRepository extends RepositoryBase<ExternalDocTypeAttributeList, ExternalDocTypeAttributeList> implements CriteriaSupport<ExternalDocTypeAttributeList> {

    public abstract List<ExternalDocTypeAttributeList> findByAttributeName(String attributeName);

    public abstract void removeByAttributeName(String attributeName);

    public void removeAll() {
        entityManager().createNativeQuery("delete from external_doc_type_attribute_list").executeUpdate();
    }
}
