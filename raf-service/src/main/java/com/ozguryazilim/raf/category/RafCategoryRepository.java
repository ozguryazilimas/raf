package com.ozguryazilim.raf.category;

import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.telve.data.TreeRepositoryBase;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 *
 * @author oyas
 */
@Repository
@Dependent
public abstract class RafCategoryRepository extends TreeRepositoryBase<RafCategory> implements CriteriaSupport<RafCategory>{
    
    /**
     * Geriye yeni bir RafCategory oluşturup döndürür.
     *
     * @param parent
     * @return
     */
    public RafCategory newRafCategory(RafCategory  parent) {
        RafCategory entity = new RafCategory();
        entity.setParent(parent);
        return entity;
    }
}
