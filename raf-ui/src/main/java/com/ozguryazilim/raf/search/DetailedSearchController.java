package com.ozguryazilim.raf.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.elasticsearch.search.ElasticSearchService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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

    @Inject
    RafService rafService;

    private DetailedSearchModel searchModel;
    private SearchResultDataModel searchResult;

    private static final String DEFAULT_TAB_NAME = "genericSearchPanelController";
    private String activeSearchPanelController = DEFAULT_TAB_NAME;

    private List<RafDefinition> rafList;

    private Map<String, String> extendedColumnMap = new HashMap();

    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private static final String PROP_RECORD_TYPE = "raf:recordType";
    private static final String PROP_DOCUMENT_TYPE = "raf:documentType";
    private static final String PROP_RECORD_NO = "raf:recordNo";
    private static final String PROP_TITLE = "jcr:title";
    private static final String PROP_DESCRIPTON = "jcr:description";

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

    public Map<String, String> getExtendedColumnMap() {
        return extendedColumnMap;
    }

    public void setExtendedColumnMap(Map<String, String> extendedColumnMap) {
        this.extendedColumnMap = extendedColumnMap;
    }

    public Object getMetaDataValue(RafObject rafObject, String metaDataTag) {
        if (PROP_TITLE.equals(metaDataTag)) {
            return rafObject.getName();
        } else if (PROP_DOCUMENT_TYPE.equals(metaDataTag) && rafObject instanceof RafRecord) {
            return ((RafRecord) rafObject).getDocumentType();
        } else if (PROP_RECORD_NO.equals(metaDataTag) && rafObject instanceof RafRecord) {
            return ((RafRecord) rafObject).getRecordNo();
        } else if (PROP_RECORD_TYPE.equals(metaDataTag) && rafObject instanceof RafRecord) {
            return ((RafRecord) rafObject).getRecordType();
        } else if (PROP_DESCRIPTON.equals(metaDataTag) && rafObject instanceof RafRecord) {
            return ((RafRecord) rafObject).getInfo();
        }
        if (metaDataTag.contains("externalDocMetaTag:externalDocTypeAttribute")) {
            for (RafMetadata metadata : rafObject.getMetadatas()) {
                if (metadata.getAttributes().containsKey("externalDocMetaTag:externalDocTypeAttribute")) {
                    String[] attrNames = metadata.getAttributes().get("externalDocMetaTag:externalDocTypeAttribute").toString().split(";");
                    String[] attrValues = metadata.getAttributes().get("externalDocMetaTag:value").toString().split(";");
                    if (attrNames != null && attrValues != null) {
                        String[] metaDataTagAttrNames = metaDataTag.split(":");
                        String attrName = metaDataTagAttrNames[metaDataTagAttrNames.length - 1];
                        int i = 0;
                        for (String attr : attrNames) {
                            if (attr.equals(attrName) && attrValues.length > i) {
                                return attrValues[i];
                            }
                            i++;
                        }
                    }
                }
            }
        }
        for (RafMetadata metadata : rafObject.getMetadatas()) {
            if (metadata.getAttributes().containsKey(metaDataTag)) {
                Object mVal = metadata.getAttributes().get(metaDataTag);
                if (mVal instanceof Date) {
                    return sdf.format((Date) mVal);
                } else {
                    return metadata.getAttributes().get(metaDataTag);
                }
            }
        }

        return null;
    }

}
