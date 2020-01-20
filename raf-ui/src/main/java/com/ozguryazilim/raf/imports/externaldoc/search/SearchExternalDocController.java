package com.ozguryazilim.raf.imports.externaldoc.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.entities.ExternalDoc;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeValue;
import com.ozguryazilim.raf.externalappimport.ExternalDocRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocTypeAttributeValueRepository;
import com.ozguryazilim.raf.externalappimport.ExternalDocTypeRepository;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class SearchExternalDocController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SearchExternalDocController.class);

    private String documentName;
    private Date registerDateFrom;
    private Date registerDateTo;
    private String documentType;
    private List<ExternalDocType> documentTypes;
    private List<ExternalDocTypeAttributeValue> attributes;
    private Map<String, Object> mapAttValue;
    private SearchExternalDocDataModel searchExternalDocDataModel;

    @Inject
    ExternalDocRepository externalDocRepository;

    @Inject
    ExternalDocTypeRepository externalDocTypeRepository;

    @Inject
    ExternalDocTypeAttributeRepository externalDocTypeAttributeRepository;

    @Inject
    ExternalDocTypeAttributeValueRepository externalDocTypeAttributeValueRepository;

    public Map<String, Object> getMapAttValue() {
        return mapAttValue;
    }

    @PostConstruct
    public void init() {
        documentTypes = externalDocTypeRepository.findAll();
        listDocTypeAttributes(null);
    }

    void listDocTypeAttributes(ExternalDocType externalDocType) {
        List<ExternalDocTypeAttribute> externalDocTypeAttributes;
        if (externalDocType == null) {
            externalDocTypeAttributes = externalDocTypeAttributeRepository.findAll();
        } else {
            externalDocTypeAttributes = externalDocTypeAttributeRepository.findByDocumentType(externalDocType);
        }
        attributes = new ArrayList();
        mapAttValue = new HashMap();
        for (ExternalDocTypeAttribute attr : externalDocTypeAttributes) {
            ExternalDocTypeAttributeValue val = new ExternalDocTypeAttributeValue();
            val.setDocumentType(attr.getDocumentType());
            val.setExternalDocTypeAttribute(attr);
            attributes.add(val);
            mapAttValue.put(getMapKey(attr), null);
        }
    }

    public String getMapKey(ExternalDocTypeAttribute attribute) {
        return attribute.getDocumentType().getDocumentType().concat("_").concat(attribute.getAttributeName());
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Date getRegisterDateFrom() {
        return registerDateFrom;
    }

    public void setMapAttValue(Map<String, Object> mapAttValue) {
        this.mapAttValue = mapAttValue;
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

    public List<ExternalDocType> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<ExternalDocType> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public List<ExternalDocTypeAttributeValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ExternalDocTypeAttributeValue> attributes) {
        this.attributes = attributes;
    }

    public void onDocumentTypeChange() {
        if (!Strings.isNullOrEmpty(documentType)) {
            listDocTypeAttributes(externalDocTypeRepository.findByDocumentType(documentType).get(0));
        }
    }

    public void search() {
        searchExternalDocDataModel = new SearchExternalDocDataModel(externalDocRepository, documentName, registerDateFrom, registerDateTo, documentType, mapAttValue);
    }

    public SearchExternalDocDataModel getSearchExternalDocDataModel() {
        return searchExternalDocDataModel;
    }

    public void setSearchExternalDocDataModel(SearchExternalDocDataModel searchExternalDocDataModel) {
        this.searchExternalDocDataModel = searchExternalDocDataModel;
    }

    public String getFileName(String rafFilePath) {
        return rafFilePath.contains("/") ? rafFilePath.substring(rafFilePath.lastIndexOf("/")) : rafFilePath;
    }

    public String getRafFromPath(String rafFilePath) {
        return rafFilePath.contains("/") ? rafFilePath.split("/")[2] : rafFilePath;
    }

    public String getFileLink(ExternalDoc doc) {
        return String.format("/dolap/raf.jsf?id=%s&o=%s", getRafFromPath(doc.getRafFilePath()), doc.getRafFileId());
    }
}
