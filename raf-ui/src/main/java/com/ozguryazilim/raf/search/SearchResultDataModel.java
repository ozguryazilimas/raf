package com.ozguryazilim.raf.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.mongo.search.MongoSearchService;
import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class SearchResultDataModel extends LazyDataModel<RafObject> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SearchResultDataModel.class);

    private List<RafObject> datasource;
    private List<RafDefinition> rafs;
    private DetailedSearchModel searchModel;
    private SearchService searchService;
    private MongoSearchService mongoSearchService;

    public SearchResultDataModel(List<RafDefinition> rafs, DetailedSearchModel searchModel, SearchService searchService, MongoSearchService mongoSearchService) {
        this.rafs = rafs;
        this.searchModel = searchModel;
        this.searchService = searchService;
        this.mongoSearchService = mongoSearchService;
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
            if (!searchModel.getSearchInDocumentName() && !Strings.isNullOrEmpty(searchModel.getSearchText())) {
                datasource = searchService.detailedSearch(searchModel, rafs, pageSize, first, sortField, sortOrder).getItems();
                this.setRowCount((int) searchService.detailedSearchCount(searchModel, rafs));//FIXME Count sorgusu çekip bildirmek gerekebilir.   
            } else {
                datasource = mongoSearchService.detailedSearch(searchModel, rafs, pageSize, first, sortField, sortOrder).getItems();
                this.setRowCount((int) mongoSearchService.detailedSearchCount(searchModel, rafs));
            }
        } catch (RafException ex) {
            LOG.error("RafException", ex);
            this.setRowCount(0);
        }
        return datasource;
    }

}
