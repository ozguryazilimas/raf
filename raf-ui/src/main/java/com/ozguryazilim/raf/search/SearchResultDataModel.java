package com.ozguryazilim.raf.search;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.elasticsearch.search.ElasticSearchService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.modeshape.jcr.JcrRepository.QueryLanguage;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class SearchResultDataModel extends LazyDataModel<RafObject> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SearchResultDataModel.class);
    private boolean elasticSearch = true;

    private List<RafObject> datasource;
    private List<RafDefinition> rafs;
    private DetailedSearchModel searchModel;
    private SearchService searchService;
    private ElasticSearchService elasticSearchService;
    private Map<String, String> columnMap = new HashMap();

    public SearchResultDataModel(List<RafDefinition> rafs, DetailedSearchModel searchModel, SearchService searchService, ElasticSearchService elasticSearchService) {
        this.columnMap.put("path", "nodes.[jcr:path]");
        this.columnMap.put("title", "nodes.[jcr:title]");
        this.columnMap.put("createBy", "nodes.[jcr:createBy]");
        this.columnMap.put("createDate", "nodes.[jcr:created]");
        this.columnMap.put("updateBy", "nodes.[jcr:lastModifiedBy]");
        this.columnMap.put("updateDate", "nodes.[jcr:lastModified]");

        this.rafs = rafs;
        this.searchModel = searchModel;
        this.searchService = searchService;
        this.elasticSearchService = elasticSearchService;
        this.elasticSearch = "elasticsearch".equals(ConfigResolver.getPropertyValue("rafSearch.provider", "modeshape"));//default search provider is modeshape
    }

    @Override

    public RafObject getRowData(String rowKey) {
        for (RafObject rec : datasource) {
            if (rec.getId().equals(rowKey)) {
                return rec;
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(RafObject car) {
        return car.getId();
    }

    @Override
    public List<RafObject> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        try {
            List<String> searchPanels = SearchRegistery.getSearchPanels();
            if (sortField != null && sortOrder != null) {
                searchModel.setSortBy(columnMap.get(sortField));
                searchModel.setSortOrder(sortOrder == SortOrder.ASCENDING ? "ASC" : "DESC");
            }
            if (!elasticSearch) {
                //default search provider elasticsearch değil veya fulltext search yapılıyor ise arama işini modeshape e yönlendir.
                List extendedQuery = new ArrayList();
                for (String searchPanel : searchPanels) {
                    SearchPanelController spc = BeanProvider.getContextualReference(searchPanel, false, SearchPanelController.class);
                    extendedQuery.addAll(spc.getSearchQuery(rafs, QueryLanguage.JCR_SQL2, searchModel));
                }
                datasource = searchService.detailedSearch(searchModel, rafs, pageSize, first, sortField, sortOrder, extendedQuery).getItems();
                this.setRowCount((int) searchService.detailedSearchCount(searchModel, rafs, extendedQuery));//FIXME Count sorgusu çekip bildirmek gerekebilir.   
            } else {
                List extendedQuery = new ArrayList();
                for (String searchPanel : searchPanels) {
                    SearchPanelController spc = BeanProvider.getContextualReference(searchPanel, false, SearchPanelController.class);
                    extendedQuery.addAll(spc.getSearchQuery(rafs, "elasticSearch", searchModel));
                }
                datasource = elasticSearchService.detailedSearch(searchModel, rafs, pageSize, first, sortField, sortOrder, extendedQuery).getItems();
                this.setRowCount((int) elasticSearchService.detailedSearchCount(searchModel, rafs, extendedQuery));
            }
        } catch (RafException ex) {
            LOG.error("RafException", ex);
            this.setRowCount(0);
        }
        return datasource;
    }

    public Map<String, String> getColumnMap() {
        return columnMap;
    }

    public void setColumnMap(Map<String, String> columnMap) {
        this.columnMap = columnMap;
    }

}
