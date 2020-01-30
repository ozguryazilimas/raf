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

    public DetailedSearchModel() {
    }

    public String getSearchRaf() {
        return searchRaf;
    }

    public String getSearchSubPath() {
        return searchSubPath;
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

}
