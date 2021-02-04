package com.ozguryazilim.raf.externaldoc.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.externaldoc.entities.ExternalDocType;
import com.ozguryazilim.raf.externaldoc.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.externaldoc.entities.ExternalDocTypeAttributeList;
import com.ozguryazilim.raf.externaldoc.repositories.ExternalDocTypeAttributeListRepository;
import com.ozguryazilim.raf.externaldoc.repositories.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.externaldoc.repositories.ExternalDocTypeRepository;
import java.io.Serializable;
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
 * @author oyasc34
 */
@WindowScoped
@Named
public class ExternalDocSearchController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalDocSearchController.class);

    private List<ExternalDocType> documentTypes;
    private List<ExternalDocTypeAttribute> attributes;
    private Map<String, List<ExternalDocTypeAttributeList>> listValueCache;

    private Date registerDateFrom;
    private Date registerDateTo;

    private String documentType;
    private String documentStatus;

    private Map<String, Object> mapAttValue;

    private Map<String, Object> mapWFAttValue;

    @Inject
    ExternalDocTypeRepository externalDocTypeRepository;

    @Inject
    ExternalDocTypeAttributeRepository externalDocTypeAttributeRepository;

    @Inject
    ExternalDocTypeAttributeListRepository externalDocTypeAttributeListRepository;

    public List<ExternalDocTypeAttribute> getAttributes() {
        return attributes;
    }

    public List<ExternalDocType> getDocumentTypes() {
        return documentTypes;
    }

    public List<ExternalDocTypeAttributeList> getListedAttributeValues(ExternalDocTypeAttribute attribute) {
        return listValueCache.get(attribute.getAttributeName());
    }

    @PostConstruct
    public void init() {
        documentTypes = externalDocTypeRepository.findAll();
        listValueCache = new HashMap();
    }

    void listDocTypeAttributes(ExternalDocType externalDocType) {
        if (externalDocType == null) {
            attributes = externalDocTypeAttributeRepository.findAll();
        } else {
            attributes = externalDocTypeAttributeRepository.findByDocumentType(externalDocType);
        }
        setMapAttValue(new HashMap());
        for (ExternalDocTypeAttribute attr : attributes) {
            listValueCache.put(attr.getAttributeName(), externalDocTypeAttributeListRepository.findByAttributeName(attr.getAttributeName()));
            getMapAttValue().put(getMapKey(attr), null);
        }
    }

    public void onDocumentTypeChange() {
        if (!Strings.isNullOrEmpty(documentType)) {
            listDocTypeAttributes(externalDocTypeRepository.findByDocumentType(documentType).get(0));
        }
    }

    public String getMapKey(ExternalDocTypeAttribute attribute) {
        return attribute.getDocumentType().getDocumentType().replaceAll(":", "").concat(":").concat(attribute.getAttributeName());
    }

    public void setAttributes(List<ExternalDocTypeAttribute> attributes) {
        this.attributes = attributes;
    }

    public void setDocumentTypes(List<ExternalDocType> documentTypes) {
        this.documentTypes = documentTypes;
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

    public Map<String, Object> getMapWFAttValue() {
        return mapWFAttValue;
    }

    public void setMapWFAttValue(Map<String, Object> mapWFAttValue) {
        this.mapWFAttValue = mapWFAttValue;
    }

}
