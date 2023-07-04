package com.ozguryazilim.raf.record.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.record.model.RafRecordType;
import com.ozguryazilim.raf.record.ui.RecordController;
import com.ozguryazilim.raf.search.DetailedSearchController;
import com.ozguryazilim.raf.search.SearchPanelController;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanelRegistery;
import com.ozguryazilim.raf.ui.base.metadatapanels.DynaFormMetadataPanel;
import com.ozguryazilim.telve.entities.SuggestionItem;
import com.ozguryazilim.telve.suggestion.SuggestionRepository;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.query.JcrTypeSystem;
import org.modeshape.jcr.query.QueryBuilder;
import org.modeshape.jcr.query.model.QueryCommand;

/**
 *
 * @author oyasc34
 */
@WindowScoped
@Named
public class RecordSearchPanelController implements SearchPanelController, Serializable {

    private Short order = 0;

    @Inject
    DetailedSearchController detailedSearchController;

    @Inject
    private SuggestionRepository suggestionRepository;

    @Inject
    RecordController recordController;

    private String recordType;
    private List<Field> metaDataFields;

    private static final String PROP_RECORD_TYPE = "raf:recordType";
    private static final String PROP_DOCUMENT_TYPE = "raf:documentType";
    private static final String PROP_RECORD_NO = "raf:recordNo";
    private static final String PROP_TITLE = "jcr:title";
    private static final String PROP_DESCRIPTON = "jcr:description";

    private static final String RECORD_TABLE_ALIAS = "record";

    @PostConstruct
    public void init() {
        this.setOrder((short) 1);
    }

    public String getRecordType() {
        return recordType;
    }

    public void onRecordTypeChange() {
        if (detailedSearchController.getSearchModel() != null && !Strings.isNullOrEmpty(recordType)) {
            detailedSearchController.getSearchModel().setRecordType(recordType.split(";")[0]);
            detailedSearchController.getSearchModel().setRecordMetaDataName(recordType.split(";")[1]);
            prepareMetaDataPanel();
        }
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public List<Field> getMetaDataFields() {
        return metaDataFields;
    }

    public void setMetaDataFields(List<Field> metaDataFields) {
        this.metaDataFields = metaDataFields;
    }

    private void prepareMetaDataPanel() {
        metaDataFields = new ArrayList();
        detailedSearchController.getSearchModel().setMapWFAttValue(new HashMap());
        List<AbstractMetadataPanel> ls = MetadataPanelRegistery.getPanels(detailedSearchController.getSearchModel().getRecordMetaDataName());
        for (AbstractMetadataPanel l : ls) {
            if (l instanceof DynaFormMetadataPanel) {
                DynaFormMetadataPanel panel = ((DynaFormMetadataPanel) l);
                if (panel.getForm() != null) {
                    metaDataFields = panel.getForm().getFields();
                    metaDataFields.forEach((mdf) -> {
                        if ("Suggestion".equals(mdf.getType()) || "Text".equals(mdf.getType()) || "RafFolder".equals(mdf.getType())) {
                            detailedSearchController.getSearchModel().getMapWFAttValue().put(mdf.getDataKey(), "");
                        } else if ("Date".equals(mdf.getType())) {
                            detailedSearchController.getSearchModel().getMapWFAttValue().put(mdf.getDataKey(), null);
                        }
                    });
                }
            }
        }

        detailedSearchController.setExtendedColumnMap(new HashMap());
        detailedSearchController.getExtendedColumnMap().put(PROP_TITLE, "process.label.Subject");
        detailedSearchController.getExtendedColumnMap().put(PROP_RECORD_NO, "record.label.RecordNo");
        detailedSearchController.getExtendedColumnMap().put(PROP_DOCUMENT_TYPE, "process.label.DocumentType");
        for (Field metaDataField : metaDataFields) {
            detailedSearchController.getExtendedColumnMap().put(metaDataField.getDataKey(), metaDataField.getLabel());
        }
    }

    public List<String> getSuggestion(String group) {
        return suggestionRepository.findByGroup(group).stream().map(SuggestionItem::getData)
                .distinct().collect(Collectors.toList());
    }

    @Override
    public String getTabFragment() {
        return "/record/search/recordSearchPage.xhtml";
    }

    @Override
    public String getExternalFragments() {
        return null;
    }

    @Override
    public void changeEvent() {

        if (detailedSearchController.getSearchModel().getRecordType() != null && !detailedSearchController.getSearchModel().getRecordType().isEmpty()) {
            setRecordType(detailedSearchController.getSearchModel().getRecordType());
            prepareMetaDataPanel();

        }
    }

    @Override
    public void clearEvent() {
        setRecordType(null);
        detailedSearchController.getSearchModel().setRecordType(null);
        detailedSearchController.getSearchModel().setRecordMetaDataName(null);
        detailedSearchController.getSearchModel().setMapWFAttValue(new HashMap());
        metaDataFields = new ArrayList();
    }

    public void saveRecordPathQueryCommand(DetailedSearchModel searchModel) {

        QueryBuilder builder = new QueryBuilder(new ExecutionContext().getValueFactories().getTypeSystem());

        boolean recordTypePresent           = !Strings.isNullOrEmpty(searchModel.getRecordType());
        boolean recordDocumentTypePresent   = !Strings.isNullOrEmpty(searchModel.getRecordDocumentType());
        boolean recordNoPresent             = !Strings.isNullOrEmpty(searchModel.getRecordNo());
        boolean recordTitlePresent          = !Strings.isNullOrEmpty(searchModel.getTitle());
        boolean recordWfValuePresent        = searchModel.getMapWFAttValue() != null && !searchModel.getMapWFAttValue().isEmpty();

        builder.select("jcr:path")
                .from("raf:record as record");

        if (recordWfValuePresent) {
            String childTable = searchModel.getRecordMetaDataName();
            builder
                    .join(childTable)
                    .onChildNode(RECORD_TABLE_ALIAS, childTable);
        }

        QueryBuilder.ConstraintBuilder recordConstraintBuilder = builder.where();
        if (recordTypePresent) {
            recordConstraintBuilder
                    .propertyValue(RECORD_TABLE_ALIAS, PROP_RECORD_TYPE)
                    .isEqualTo(searchModel.getRecordType());
        }

        if (recordDocumentTypePresent) {
            recordConstraintBuilder
                    .propertyValue(RECORD_TABLE_ALIAS, PROP_DOCUMENT_TYPE)
                    .isEqualTo(searchModel.getRecordDocumentType());
        }

        if (recordNoPresent) {
            recordConstraintBuilder
                    .propertyValue(RECORD_TABLE_ALIAS, PROP_RECORD_NO)
                    .isEqualTo(searchModel.getRecordNo());
        }

        if (recordTitlePresent) {
            recordConstraintBuilder
                    .propertyValue(RECORD_TABLE_ALIAS, PROP_TITLE)
                    .isEqualTo(searchModel.getTitle());
        }

        if (recordWfValuePresent) {
            String childTable = searchModel.getRecordMetaDataName();

            for (Map.Entry<String, Object> entry : searchModel.getMapWFAttValue().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (isWfValueValid(value)) {
                    if (value instanceof String) {
                        recordConstraintBuilder.propertyValue(childTable, key).isLike().cast("%" + value.toString() + "%").asString();
                    } else if (value instanceof Date) {
                        recordConstraintBuilder.propertyValue(childTable, key).isEqualTo().cast(getJCRDate((Date) value)).asString();
                    }
                }
            }
        }

        builder = recordConstraintBuilder.end();

        searchModel.setRecordSubQuery(builder.query());
    }

    private boolean isWfValueValid(Object value) {
        return value != null && !value.toString().trim().isEmpty();
    }

    @Override
    public List getSearchQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel) {

        if ("JCR-SQL2".equals(queryLanguage)) {
            List<String> whereExpressions = new ArrayList();

            if (!Strings.isNullOrEmpty(searchModel.getRecordType())) {
                whereExpressions.add(" nodes.[" + PROP_RECORD_TYPE + "] = '" + searchModel.getRecordType() + "' ");
            }

            if (!Strings.isNullOrEmpty(searchModel.getRecordDocumentType())) {
                whereExpressions.add(" nodes.[" + PROP_DOCUMENT_TYPE + "] = '" + searchModel.getRecordDocumentType() + "' ");
            }

            if (!Strings.isNullOrEmpty(searchModel.getRecordNo())) {
                whereExpressions.add(" nodes.[" + PROP_RECORD_NO + "] = '" + searchModel.getRecordNo() + "' ");
            }

            if (!Strings.isNullOrEmpty(searchModel.getTitle())) {
                whereExpressions.add(" nodes.[" + PROP_TITLE + "] = '" + searchModel.getTitle() + "' ");
            }

            String metadataName = "";
            if (searchModel.getMapWFAttValue() != null && !searchModel.getMapWFAttValue().isEmpty()) {
                for (Map.Entry<String, Object> entry : searchModel.getMapWFAttValue().entrySet()) {
                    metadataName = searchModel.getRecordMetaDataName().split(":")[0];
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value != null && !value.toString().trim().isEmpty()) {
                        if (value instanceof String) {
                            whereExpressions.add(" " + metadataName.concat(".[").concat(key).concat("]") + " LIKE '%" + value.toString() + "%'");
                        } else if (value instanceof Date) {
                            whereExpressions.add(" " + metadataName.concat(".[").concat(key).concat("]") + " = " + getJCRDate((Date) value) + "");
                        }
                    }
                }
            }

            saveRecordPathQueryCommand(searchModel);
            return whereExpressions;
        } else if ("elasticSearch".equals(queryLanguage)) {
            List mustQueryList = new ArrayList();

            if (!Strings.isNullOrEmpty(searchModel.getRecordType())) {
                Map match = new HashMap();
                Map recordType = new HashMap();
                recordType.put("recordType", searchModel.getRecordType());
                match.put("match", recordType);
                mustQueryList.add(match);
            }

            if (!Strings.isNullOrEmpty(searchModel.getRecordDocumentType())) {
                Map match = new HashMap();
                Map matchValue = new HashMap();
                matchValue.put("documentType", searchModel.getRecordDocumentType());
                match.put("match", matchValue);
                mustQueryList.add(match);

            }

            if (!Strings.isNullOrEmpty(searchModel.getRecordNo())) {
                Map match = new HashMap();
                Map matchValue = new HashMap();
                matchValue.put("recordNo", "*" + searchModel.getRecordNo() + "*");
                match.put("wildcard", matchValue);
                mustQueryList.add(match);
            }

            if (!Strings.isNullOrEmpty(searchModel.getTitle())) {
                Map match = new HashMap();
                Map matchValue = new HashMap();
                matchValue.put("title", "*" + searchModel.getTitle() + "*");
                match.put("wildcard", matchValue);
                mustQueryList.add(match);
            }

            if (searchModel.getMapWFAttValue() != null && !searchModel.getMapWFAttValue().isEmpty()) {
                for (Map.Entry<String, Object> entry : searchModel.getMapWFAttValue().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value != null && !value.toString().trim().isEmpty()) {
                        if (value instanceof String) {
                            Arrays.asList(value.toString().split(" ")).forEach((str) -> {
                                Map wildcard = new HashMap();
                                Map filePath = new HashMap();
                                filePath.put(key, "*" + str + "*");
                                wildcard.put("wildcard", filePath);
                                mustQueryList.add(wildcard);
                            });

                        } else if (value instanceof Date) {
                            Map match = new HashMap();
                            Map filePath = new HashMap();
                            filePath.put(key, value);
                            match.put("match", filePath);
                            mustQueryList.add(match);
                        }
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

    public RafRecordType getRecrodTypeByRecordTypeName(String recordName) {
        for (RafRecordType rt : recordController.getAllRecordTypes()) {
            if (recordName.split(";")[0].equals(rt.getName())) {
                return rt;
            }
        }
        return new RafRecordType();
    }

    @Override
    public List getSearchSortQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel) {
        if (searchModel != null && !Strings.isNullOrEmpty(searchModel.getSortBy()) && !Strings.isNullOrEmpty(searchModel.getSortOrder()) && !Strings.isNullOrEmpty(searchModel.getRecordType())) {
            if ("JCR-SQL2".equals(queryLanguage)) {
                List<String> orderExpression = new ArrayList();
                Map<String, String> mapSort = new HashMap();
                mapSort.put(PROP_TITLE, "nodes.[jcr:title]");
                mapSort.put(PROP_RECORD_NO, "nodes.[" + PROP_RECORD_NO + "]");
                mapSort.put(PROP_DOCUMENT_TYPE, "nodes.[" + PROP_DOCUMENT_TYPE + "]");
                String metadataName = searchModel.getRecordMetaDataName().split(":")[0];
                for (Field metaDataField : metaDataFields) {
                    mapSort.put(metaDataField.getDataKey(), " " + metadataName.concat(".[").concat(metaDataField.getDataKey()).concat("]"));
                }
                if (mapSort.containsKey(searchModel.getSortBy())) {
                    orderExpression.add(mapSort.get(searchModel.getSortBy()).concat(" ").concat(searchModel.getSortOrder()));
                }
                return orderExpression;
            } else if ("elasticSearch".equals(queryLanguage)) {
                Map<String, String> mapSort = new HashMap();
                mapSort.put(PROP_TITLE, "title");
                mapSort.put(PROP_RECORD_NO, "recordNo");
                mapSort.put(PROP_DOCUMENT_TYPE, "documentType");
                for (Field metaDataField : metaDataFields) {
                    mapSort.put(metaDataField.getDataKey(), metaDataField.getDataKey());
                }
                List sortList = new ArrayList();
                if (mapSort.containsKey(searchModel.getSortBy())) {
                    Map order = new HashMap();
                    Map sortBy = new HashMap();
                    order.put("order", searchModel.getSortOrder().toLowerCase());
                    sortBy.put(mapSort.get(searchModel.getSortBy()), order);
                    sortList.add(sortBy);
                }
                return sortList;
            }
        }
        return new ArrayList();
    }

}
