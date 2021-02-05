package com.ozguryazilim.raf.record.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.models.DetailedSearchModel;
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

    private String recordType;
    private List<Field> metaDataFields;

    private static final String PROP_RECORD_TYPE = "raf:recordType";

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

    @Override
    public List getSearchQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel) {
        if ("JCR-SQL2".equals(queryLanguage)) {
            List<String> whereExpressions = new ArrayList();

            if (!Strings.isNullOrEmpty(searchModel.getRecordType())) {
                whereExpressions.add(" nodes.[" + PROP_RECORD_TYPE + "] = '" + searchModel.getRecordType() + "' ");
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
}
