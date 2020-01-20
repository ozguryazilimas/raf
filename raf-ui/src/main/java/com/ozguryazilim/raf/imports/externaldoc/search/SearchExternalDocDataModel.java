package com.ozguryazilim.raf.imports.externaldoc.search;

import com.ozguryazilim.raf.entities.ExternalDoc;
import com.ozguryazilim.raf.externalappimport.ExternalDocRepository;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 *
 * @author oyas
 */
public class SearchExternalDocDataModel extends LazyDataModel<ExternalDoc> {

    private List<ExternalDoc> datasource;

    private ExternalDocRepository externalDocRepository;
    private String documentName;
    private Date registerDateFrom;
    private Date registerDateTo;
    private String documentType;
    private Map<String, Object> mapAttributeValue;

    public SearchExternalDocDataModel(ExternalDocRepository externalDocRepository, String documentName, Date registerDateFrom, Date registerDateTo, String documentType, Map<String, Object> mapAttributeValue) {
        this.externalDocRepository = externalDocRepository;
        this.documentName = documentName;
        this.registerDateFrom = registerDateFrom;
        this.registerDateTo = registerDateTo;
        this.documentType = documentType;
        this.mapAttributeValue = mapAttributeValue;
    }

    @Override
    public ExternalDoc getRowData(String rowKey) {
        for (ExternalDoc rec : datasource) {
            if (rec.getId().equals(rowKey)) {
                return rec;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(ExternalDoc rec) {
        return rec.getId();
    }

    @Override
    public List<ExternalDoc> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        datasource = externalDocRepository.searchNative(documentName, documentType, registerDateFrom, registerDateTo, mapAttributeValue, first, pageSize);
        this.setRowCount(externalDocRepository.countNative(documentName, documentType, registerDateFrom, registerDateTo, mapAttributeValue));
        return datasource;
    }

}
