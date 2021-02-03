package com.ozguryazilim.raf.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.elasticsearch.search.ElasticSearchService;
import com.ozguryazilim.raf.entities.ExternalDocType;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.entities.ExternalDocTypeAttributeList;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeAttributeListRepository;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.externaldoc.ExternalDocTypeRepository;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanelRegistery;
import com.ozguryazilim.raf.ui.base.metadatapanels.DynaFormMetadataPanel;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.entities.SuggestionItem;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
import com.ozguryazilim.telve.suggestion.SuggestionRepository;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.primefaces.event.SelectEvent;
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

    private List<RafDefinition> rafList;

    private List<ExternalDocType> documentTypes;
    private List<ExternalDocTypeAttribute> attributes;
    private DetailedSearchModel searchModel;
    private Map<String, List<ExternalDocTypeAttributeList>> listValueCache;
    private SearchResultDataModel searchResult;
    private List<Field> metaDataFields;
    private String recordType;

    @Inject
    ExternalDocTypeRepository externalDocTypeRepository;

    @Inject
    ExternalDocTypeAttributeRepository externalDocTypeAttributeRepository;

    @Inject
    ExternalDocTypeAttributeListRepository externalDocTypeAttributeListRepository;

    @Inject
    private SuggestionRepository suggestionRepository;

    public List<ExternalDocTypeAttribute> getAttributes() {
        return attributes;
    }

    public List<ExternalDocType> getDocumentTypes() {
        return documentTypes;
    }

    public List<ExternalDocTypeAttributeList> getListedAttributeValues(ExternalDocTypeAttribute attribute) {
        return listValueCache.get(attribute.getAttributeName());
    }

    public List<Field> getMetaDataFields() {
        return metaDataFields;
    }

    public String getRecordType() {
        return recordType;
    }

    public DetailedSearchModel getSearchModel() {
        return searchModel;
    }

    public SearchResultDataModel getSearchResult() {
        return searchResult;
    }

    @PostConstruct
    public void init() {
        clearSearch();
    }

    public void clearSearch() {
        rafList = rafDefinitionService.getRafsForUser(identity.getLoginName());
        documentTypes = externalDocTypeRepository.findAll();
        searchModel = new DetailedSearchModel();
        searchModel.setSearchInDocumentName(Boolean.TRUE);
        searchModel.setSearchInDocumentTags(Boolean.TRUE);
        listValueCache = new HashMap();
        searchResult = null;
    }

    public void searchFromFolder(RafContext context, String searchText) {
        if (context != null) {
            if (context.getSelectedObject() instanceof RafFolder) {
                searchModel.setSearchSubPath(context.getSelectedObject().getPath());
            } else if (context.getSelectedRaf() != null && context.getSelectedRaf().getNode() != null) {
                searchModel.setSearchSubPath(context.getSelectedRaf().getNode().getPath());
            }
        }
        if (searchText != null) {
            searchModel.setSearchText(searchText);
        }
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        try {
            response.sendRedirect("/dolap/search/searchPage.jsf");
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }
    }

    public void selectFolder(SelectEvent event) {
        if (searchModel != null && event != null) {
            LookupSelectTuple sl = (LookupSelectTuple) event.getObject();
            if (sl != null) {
                searchModel.setSearchSubPath(((RafFolder) sl.getValue()).getPath());
            }
        }
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

    public void onRecordTypeChange() {
        if (searchModel != null && !Strings.isNullOrEmpty(recordType)) {
            searchModel.setRecordType(recordType.split(";")[0]);
            searchModel.setRecordMetaDataName(recordType.split(";")[1]);
            metaDataFields = new ArrayList();
            searchModel.setMapWFAttValue(new HashMap());
            List<AbstractMetadataPanel> ls = MetadataPanelRegistery.getPanels(searchModel.getRecordMetaDataName());
            for (AbstractMetadataPanel l : ls) {
                if (l instanceof DynaFormMetadataPanel) {
                    DynaFormMetadataPanel panel = ((DynaFormMetadataPanel) l);
                    if (panel.getForm() != null) {
                        metaDataFields = panel.getForm().getFields();
                        metaDataFields.forEach((mdf) -> {
                            if ("Suggestion".equals(mdf.getType()) || "Text".equals(mdf.getType()) || "RafFolder".equals(mdf.getType())) {
                                searchModel.getMapWFAttValue().put(mdf.getDataKey(), "");
                            } else if ("Date".equals(mdf.getType())) {
                                searchModel.getMapWFAttValue().put(mdf.getDataKey(), null);
                            }
                        });
                    }
                }
            }
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

    public void setMetaDataFields(List<Field> metaDataFields) {
        this.metaDataFields = metaDataFields;
    }

    public void setRafList(List<RafDefinition> rafList) {
        this.rafList = rafList;
    }

    public void search() {
        LOG.info("Search for {}", searchModel);
        searchResult = new SearchResultDataModel(rafList, searchModel, searchService, elasticSearchService);
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
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

    public String getExternalDocCreatedBy(RafObject rafObject) {
        for (RafMetadata metadata : rafObject.getMetadatas()) {
            if ("externalDoc:metadata".equals(metadata.getType())) {
                return metadata.getAttributes().get("externalDoc:documentCreator").toString();
            }
        }
        return "";
    }

    public Date getExternalDocCreatedDate(RafObject rafObject) {
        for (RafMetadata metadata : rafObject.getMetadatas()) {
            if ("externalDoc:metadata".equals(metadata.getType())) {
                return (Date) metadata.getAttributes().get("externalDoc:documentCreateDate");
            }
        }
        return null;
    }

    public List<String> getSuggestion(String group) {
        return suggestionRepository.findByGroup(group).stream().map(SuggestionItem::getData)
                .distinct().collect(Collectors.toList());
    }

}
