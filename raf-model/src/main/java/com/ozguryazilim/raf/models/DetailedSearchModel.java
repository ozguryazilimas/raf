package com.ozguryazilim.raf.models;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class DetailedSearchModel {

    private String searchRaf;

    private String searchSubPath;

    private String searchText;

    private Date dateFrom;

    private Date dateTo;

    private Date registerDateFrom;
    private Date registerDateTo;

    private String documentType;
    private String documentStatus;

    private Map<String, Object> mapAttValue;

    private Map<String, Object> mapWFAttValue;

    private Boolean searchInDocumentName = Boolean.TRUE;

    private String sortBy;
    private String sortOrder;

    private String recordType;
    private String recordMetaDataName;

    public DetailedSearchModel() {
    }

    public Map<String, Object> getMapWFAttValue() {
        return mapWFAttValue;
    }

    public String getRecordMetaDataName() {
        return recordMetaDataName;
    }

    public Boolean getSearchInDocumentName() {
        return searchInDocumentName;
    }

    public String getSearchRaf() {
        return searchRaf;
    }

    public String getSearchSubPath() {
        return searchSubPath;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setMapWFAttValue(Map<String, Object> mapWFAttValue) {
        this.mapWFAttValue = mapWFAttValue;
    }

    public void setRecordMetaDataName(String recordMetaDataName) {
        this.recordMetaDataName = recordMetaDataName;
    }

    public void setSearchInDocumentName(Boolean searchInDocumentName) {
        this.searchInDocumentName = searchInDocumentName;
    }

    public void setSearchRaf(String searchRaf) {
        this.searchRaf = searchRaf;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchSubPath(String searchSubPath) {
        this.searchSubPath = searchSubPath;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public Date getRegisterDateFrom() {
        return registerDateFrom;
    }

    public void setRegisterDateFrom(Date registerDateFrom) {
        this.registerDateFrom = registerDateFrom;
    }

    public Date getRegisterDateTo() {
        return registerDateTo;
    }

    public void setRegisterDateTo(Date registerDateTo) {
        this.registerDateTo = registerDateTo;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Map<String, Object> getMapAttValue() {
        return mapAttValue;
    }

    public void setMapAttValue(Map<String, Object> mapAttValue) {
        this.mapAttValue = mapAttValue;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

}
