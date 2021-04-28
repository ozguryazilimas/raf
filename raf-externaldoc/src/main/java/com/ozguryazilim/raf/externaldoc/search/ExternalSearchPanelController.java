package com.ozguryazilim.raf.externaldoc.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.externaldoc.entities.ExternalDocType;
import com.ozguryazilim.raf.externaldoc.entities.ExternalDocTypeAttribute;
import com.ozguryazilim.raf.externaldoc.entities.ExternalDocTypeAttributeList;
import com.ozguryazilim.raf.externaldoc.repositories.ExternalDocTypeAttributeListRepository;
import com.ozguryazilim.raf.externaldoc.repositories.ExternalDocTypeAttributeRepository;
import com.ozguryazilim.raf.externaldoc.repositories.ExternalDocTypeRepository;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.search.DetailedSearchController;
import com.ozguryazilim.raf.search.SearchPanelController;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
public class ExternalSearchPanelController implements SearchPanelController, Serializable {

    private Short order = 0;

    private static final Logger LOG = LoggerFactory.getLogger(ExternalSearchPanelController.class);

    private List<ExternalDocType> documentTypes;
    private List<ExternalDocTypeAttribute> attributes;
    private Map<String, List<ExternalDocTypeAttributeList>> listValueCache;

    @Inject
    ExternalDocTypeRepository externalDocTypeRepository;

    @Inject
    ExternalDocTypeAttributeRepository externalDocTypeAttributeRepository;

    @Inject
    ExternalDocTypeAttributeListRepository externalDocTypeAttributeListRepository;

    @Inject
    DetailedSearchController detailedSearchController;

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
        this.setOrder((short) 2);
        clearEvent();        
    }

    void listDocTypeAttributes(ExternalDocType externalDocType) {
        if (externalDocType == null) {
            attributes = externalDocTypeAttributeRepository.findAll();
        } else {
            attributes = externalDocTypeAttributeRepository.findByDocumentType(externalDocType);
        }
        detailedSearchController.getSearchModel().setMapAttValue(new HashMap());
        for (ExternalDocTypeAttribute attr : attributes) {
            listValueCache.put(attr.getAttributeName(), externalDocTypeAttributeListRepository.findByAttributeName(attr.getAttributeName()));
            detailedSearchController.getSearchModel().getMapAttValue().put(getMapKey(attr), null);
        }
    }

    public void onDocumentTypeChange() {
        if (!Strings.isNullOrEmpty(detailedSearchController.getSearchModel().getDocumentType())) {
            listDocTypeAttributes(externalDocTypeRepository.findByDocumentType(detailedSearchController.getSearchModel().getDocumentType()).get(0));
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

    @Override
    public String getTabFragment() {
        return "/externaldoc/search/externalDocSearchPage.xhtml";
    }

    @Override
    public void changeEvent() {
        if (detailedSearchController.getSearchModel().getDocumentType() != null && !detailedSearchController.getSearchModel().getDocumentType().isEmpty()) {
            onDocumentTypeChange();
        }
    }

    @Override
    public void clearEvent() {
        documentTypes = externalDocTypeRepository.findAll();
        listValueCache = new HashMap();
        attributes = new ArrayList();
    }

    @Override
    public List getSearchQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        if ("JCR-SQL2".equals(queryLanguage)) {
            List<String> whereExpressions = new ArrayList();
            if (!Strings.isNullOrEmpty(searchModel.getDocumentType())) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentType] LIKE '%s' ", searchModel.getDocumentType()));
            }

            if (!Strings.isNullOrEmpty(searchModel.getDocumentStatus())) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentStatus] LIKE '%s'", searchModel.getDocumentStatus()));
            }

            if (searchModel.getRegisterDateFrom() != null) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentCreateDate] >= %s", getJCRDate(searchModel.getRegisterDateFrom())));
            }

            if (searchModel.getRegisterDateTo() != null) {
                whereExpressions.add(String.format(" exdoc.[externalDoc:documentCreateDate] <= %s", getJCRDate(searchModel.getRegisterDateTo())));
            }

            if (searchModel.getMapAttValue() != null && !searchModel.getMapAttValue().isEmpty()) {
                for (Map.Entry<String, Object> entry : searchModel.getMapAttValue().entrySet()) {
                    String key = entry.getKey().split(":")[1];
                    Object value = entry.getValue();
                    String valueStr = "";
                    if (value != null && !value.toString().trim().isEmpty()) {
                        if (value instanceof Date) {
                            valueStr = sdf.format((Date) value);
                        } else {
                            valueStr = value.toString();
                        }
                        whereExpressions.add(String.format(" meta.[externalDocMetaTag:externalDocTypeAttribute] LIKE '%%%s%%' AND meta.[externalDocMetaTag:value] LIKE '%%%s%%' ",
                                key, escapeQueryParam(valueStr)));
                    }
                }
            }

            return whereExpressions;
        } else if ("elasticSearch".equals(queryLanguage)) {
            List mustQueryList = new ArrayList();

            if (!Strings.isNullOrEmpty(searchModel.getDocumentType())) {
                Arrays.asList(searchModel.getDocumentType().split(" ")).forEach((str) -> {
                    Map wildcard = new HashMap();
                    Map filePath = new HashMap();
                    filePath.put("externalDoc:documentType", "*" + str + "*");
                    wildcard.put("wildcard", filePath);
                    mustQueryList.add(wildcard);
                });
            }

            if (!Strings.isNullOrEmpty(searchModel.getDocumentStatus())) {
                Arrays.asList(searchModel.getDocumentStatus().split(" ")).forEach((str) -> {
                    Map wildcard = new HashMap();
                    Map filePath = new HashMap();
                    filePath.put("externalDoc:documentStatus", "*" + str + "*");
                    wildcard.put("wildcard", filePath);
                    mustQueryList.add(wildcard);
                });
            }

            if (searchModel.getRegisterDateFrom() != null) {
                Map range = new HashMap();
                Map createDate = new HashMap();
                Map gte = new HashMap();
                gte.put("gte", searchModel.getRegisterDateFrom().getTime());
                createDate.put("externalDoc:documentCreateDate", gte);
                range.put("range", createDate);
                mustQueryList.add(range);
            }

            if (searchModel.getRegisterDateTo() != null) {
                Map range = new HashMap();
                Map createDate = new HashMap();
                Map lte = new HashMap();
                lte.put("lte", searchModel.getRegisterDateTo().getTime());
                createDate.put("externalDoc:documentCreateDate", lte);
                range.put("range", createDate);
                mustQueryList.add(range);
            }

            if (searchModel.getMapAttValue() != null && !searchModel.getMapAttValue().isEmpty()) {
                for (Map.Entry<String, Object> entry : searchModel.getMapAttValue().entrySet()) {
                    String key = entry.getKey().split(":")[1];
                    Object value = entry.getValue();
                    String valueStr = "";
                    if (value != null && !value.toString().trim().isEmpty()) {
                        if (value instanceof Date) {
                            valueStr = sdf.format((Date) value);
                        } else {
                            valueStr = value.toString();
                        }

                        Arrays.asList(key.split(" ")).forEach((str) -> {
                            Map wildcard = new HashMap();
                            Map filePath = new HashMap();
                            filePath.put("externalDocMetaTag:externalDocTypeAttribute", "*" + str + "*");
                            wildcard.put("wildcard", filePath);
                            mustQueryList.add(wildcard);
                        });

                        Arrays.asList(valueStr.split(" ")).forEach((str) -> {
                            Map wildcard = new HashMap();
                            Map filePath = new HashMap();
                            filePath.put("externalDocMetaTag:value", "*" + str + "*");
                            wildcard.put("wildcard", filePath);
                            mustQueryList.add(wildcard);
                        });
                    }
                }
            }

            return mustQueryList;
        }

        return new ArrayList();
    }

    public String getJCRDate(Date dt) {
        //yyyy-MM-ddTHH:MM:ss.000Z
        SimpleDateFormat sdfForDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfForTime = new SimpleDateFormat("HH:mm:ss");
        return "CAST('" + sdfForDate.format(dt).concat("T").concat(sdfForTime.format(dt)) + ".000Z' AS DATE)";
    }

    private String escapeQueryParam(String param) {
        return param.replace("'", "\\'");
    }

    @Override
    public void setOrder(Short order) {
        this.order = order;
    }

    @Override
    public Short getOrder() {
        return order;
    }

}
