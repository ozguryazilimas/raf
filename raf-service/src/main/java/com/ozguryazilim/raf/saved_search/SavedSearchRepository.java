package com.ozguryazilim.raf.saved_search;

import com.ozguryazilim.raf.entities.SavedSearch;
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
public abstract class SavedSearchRepository extends RepositoryBase<SavedSearch, SavedSearch> implements CriteriaSupport<SavedSearch> {

    public abstract List<SavedSearch> findByMemberName(String memberName);

    public abstract void removeByMemberName(String memberName);

    public abstract void removeByMemberNameAndSearchName(String memberName, String searchName);
}
