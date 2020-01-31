package com.ozguryazilim.raf.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeList;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeAttributeListRepository;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeRepository;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
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

    private List<RafDefinition> rafList;

    private List<ExternalDocType> documentTypes;
    private List<ExternalDocTypeAttribute> attributes;
    private DetailedSearchModel searchModel;
    private Map<String, List<ExternalDocTypeAttributeList>> listValueCache;
    private SearchResultDataModel searchResult;

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

    public DetailedSearchModel getSearchModel() {
        return searchModel;
    }

    public SearchResultDataModel getSearchResult() {
        return searchResult;
    }

    @PostConstruct
    public void init() {
        rafList = rafDefinitionService.getRafsForUser(identity.getLoginName());
        documentTypes = externalDocTypeRepository.findAll();
        searchModel = new DetailedSearchModel();
        listValueCache = new HashMap();
    }

    public String getMapKey(ExternalDocTypeAttribute attribute) {
        return attribute.getDocumentType().getDocumentType().replaceAll(":", "").concat(":").concat(attribute.getAttributeName());
    }

    void listDocTypeAttributes(ExternalDocType externalDocType) {
        if (externalDocType == null) {
            attributes = externalDocTypeAttributeRepository.findAll();
        } else {
            attributes = externalDocTypeAttributeRepository.findByDocumentType(externalDocType);
        }
        searchModel.setMapAttValue(new HashMap());
        for (ExternalDocTypeAttribute attr : attributes) {
            listValueCache.put(attr.getAttributeName(), externalDocTypeAttributeListRepository.findByAttributeName(attr.getAttributeName()));
            searchModel.getMapAttValue().put(getMapKey(attr), null);
        }
    }

    public void onDocumentTypeChange() {
        if (!Strings.isNullOrEmpty(searchModel.getDocumentType())) {
            listDocTypeAttributes(externalDocTypeRepository.findByDocumentType(searchModel.getDocumentType()).get(0));
        }
    }

    public List<RafDefinition> getRafList() {
        return rafList;
    }

    public void setAttributes(List<ExternalDocTypeAttribute> attributes) {
        this.attributes = attributes;
    }

    public void setDocumentTypes(List<ExternalDocType> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public void setRafList(List<RafDefinition> rafList) {
        this.rafList = rafList;
    }

    public void search() {
        LOG.info("Search for {}", searchModel);
        searchResult = new SearchResultDataModel(rafList, searchModel, searchService);
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
}
