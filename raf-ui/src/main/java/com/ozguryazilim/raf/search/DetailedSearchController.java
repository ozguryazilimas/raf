package com.ozguryazilim.raf.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeRepository;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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

    private RafCollection searchResult;

    @Inject
    ExternalDocTypeRepository externalDocTypeRepository;

    @Inject
    ExternalDocTypeAttributeRepository externalDocTypeAttributeRepository;

    public List<ExternalDocTypeAttribute> getAttributes() {
        return attributes;
    }

    public List<ExternalDocType> getDocumentTypes() {
        return documentTypes;
    }

    public DetailedSearchModel getSearchModel() {
        return searchModel;
    }

    public RafCollection getSearchResult() {
        return searchResult;
    }

    @PostConstruct
    public void init() {
        rafList = rafDefinitionService.getRafsForUser(identity.getLoginName());
        documentTypes = externalDocTypeRepository.findAll();
        searchModel = new DetailedSearchModel();
        listDocTypeAttributes(null);
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

        try {
            setSearchResult(searchService.detailedSearch(searchModel, rafList));

            LOG.info("Results : {}", getSearchResult());

        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Search Exception", ex);
            FacesMessages.error("Sorgu yapılamadı", ex.getLocalizedMessage());
        }
    }

    public void setSearchModel(DetailedSearchModel searchModel) {
        this.searchModel = searchModel;
    }

    public void setSearchResult(RafCollection searchResult) {
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
