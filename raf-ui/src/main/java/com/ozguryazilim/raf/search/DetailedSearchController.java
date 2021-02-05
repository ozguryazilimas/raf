package com.ozguryazilim.raf.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.elasticsearch.search.ElasticSearchService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class DetailedSearchController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(DetailedSearchController.class);

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private Identity identity;

    @Inject
    private SearchService searchService;

    @Inject
    private ElasticSearchService elasticSearchService;

    private DetailedSearchModel searchModel;
    private SearchResultDataModel searchResult;

    private static final String DEFAULT_TAB_NAME = "genericSearchPanelController";
    private String activeSearchPanelController = DEFAULT_TAB_NAME;

    private List<RafDefinition> rafList;

    public DetailedSearchModel getSearchModel() {
        return searchModel;
    }

    public SearchResultDataModel getSearchResult() {
        return searchResult;
    }

    @PostConstruct
    public void init() {
        rafList = rafDefinitionService.getRafsForUser(identity.getLoginName());
        searchModel = new DetailedSearchModel();
        searchResult = null;
    }

    public void clearSearch() {
        searchModel = new DetailedSearchModel();
        for (String searchPanel : SearchRegistery.getSearchPanels()) {
            SearchPanelController spc = BeanProvider.getContextualReference(searchPanel, false, SearchPanelController.class);
            spc.clearEvent();
        }
    }

    public void search() {
        LOG.info("Search for {}", searchModel);
        searchResult = new SearchResultDataModel(rafList, searchModel, searchService, elasticSearchService);
    }

    public void setSearchModel(DetailedSearchModel searchModel) {
        this.searchModel = searchModel;
    }

    public void setSearchResult(SearchResultDataModel searchResult) {
        this.searchResult = searchResult;
    }

    public String getRafFromPath(String rafFilePath) {
        String[] splittedPath = rafFilePath.split("/");
        return splittedPath.length > 1 ? rafFilePath.split("/")[2] : rafFilePath;
    }

    public String getFileLink(RafObject doc) {
        return String.format("/dolap/raf.jsf?id=%s&o=%s", getRafFromPath(doc.getPath()), doc.getId());
    }

    public String getActiveSearchPanelController() {
        return activeSearchPanelController;
    }

    public void setActiveSearchPanelController(String activeSearchPanelController) {
        this.activeSearchPanelController = activeSearchPanelController;
    }

    public String getSearchTab(String tabName) {
        if (Strings.isNullOrEmpty(tabName)) {
            tabName = DEFAULT_TAB_NAME;
        }
        SearchPanelController spc = (SearchPanelController) BeanProvider.getContextualReference(tabName, false);
        return spc.getTabFragment();
    }

    public Object[] getSearchPanels() {
        return SearchRegistery.getSearchPanels().stream().sorted(new Comparator<String>() {
            @Override
            public int compare(String t, String t1) {
                SearchPanelController tspc = (SearchPanelController) BeanProvider.getContextualReference(t, false);
                SearchPanelController tspc1 = (SearchPanelController) BeanProvider.getContextualReference(t1, false);
                return tspc.getOrder().compareTo(tspc1.getOrder());
            }
        }).toArray();
    }

    public void setActiveTab() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> map = context.getExternalContext().getRequestParameterMap();
        this.setActiveSearchPanelController(map.get("activeSearchPanelController"));
    }

}
