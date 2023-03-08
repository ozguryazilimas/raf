package com.ozguryazilim.raf.search;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import java.util.List;

/**
 *
 * @author oyasc34
 */
public interface SearchPanelController {

    String getTabFragment();

    String getExternalFragments();

    void changeEvent();

    void clearEvent();

    List getSearchQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel);

    List getSearchSortQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel);

    public void setOrder(Short order);

    public Short getOrder();

}
