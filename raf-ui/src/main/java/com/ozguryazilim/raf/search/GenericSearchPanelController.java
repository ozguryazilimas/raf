package com.ozguryazilim.raf.search;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.ApplicationContstants;
import com.ozguryazilim.raf.CaseSensitiveSearchService;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.SavedSearch;
import com.ozguryazilim.raf.models.DetailedSearchModel;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.saved_search.SavedSearchService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author oyasc34
 */
@WindowScoped
@Named
public class GenericSearchPanelController implements SearchPanelController, Serializable {

    private static final String PROP_TAG = "raf:tags";
    private static final String PROP_CREATED_DATE = "jcr:created";

    private static final Logger LOG = LoggerFactory.getLogger(GenericSearchPanelController.class);

    private Short order = 0;

    @Inject
    private SavedSearchService savedSearchService;

    @Inject
    DetailedSearchController detailedSearchController;

    @Inject
    CaseSensitiveSearchService caseSensitiveSearchService;

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Inject
    private Identity identity;

    private String saveSearchName;
    private Long savedSearch;

    public String getSaveSearchName() {
        return saveSearchName;
    }

    public Long getSavedSearch() {
        return savedSearch;
    }

    public List<SavedSearch> getSavedSearchs() {
        return savedSearchService.getSavedSearchs();
    }

    public void searchFromFolder(RafContext context, String searchText) {
        if (context != null) {
            if (context.getSelectedObject() instanceof RafFolder) {
                detailedSearchController.getSearchModel().setSearchSubPath(context.getSelectedObject().getPath());
            } else if (context.getSelectedRaf() != null && context.getSelectedRaf().getNode() != null) {
                detailedSearchController.getSearchModel().setSearchSubPath(context.getSelectedRaf().getNode().getPath());
            }
        }
        if (searchText != null) {
            detailedSearchController.getSearchModel().setSearchText(searchText);
        }
        navigationParameterContext.addPageParameter("folderSearch", true);
        viewNavigationHandler.navigateTo(SearchPages.SearchPage.class);
    }

    public void selectFolder(SelectEvent event) {
        if (detailedSearchController.getSearchModel() != null && event != null) {
            LookupSelectTuple sl = (LookupSelectTuple) event.getObject();
            if (sl != null) {
                detailedSearchController.getSearchModel().setSearchSubPath(((RafFolder) sl.getValue()).getPath());
            }
        }
    }

    public void setSaveSearchName(String saveSearchName) {
        this.saveSearchName = saveSearchName;
    }

    public void setSavedSearch(Long savedSearch) {
        this.savedSearch = savedSearch;
    }

    public void saveSearch() {
        try {
            savedSearchService.saveSearch(saveSearchName, detailedSearchController.getSearchModel());
            saveSearchName = "";
            FacesMessages.info("Arama kaydedildi");
        } catch (Exception e) {
            FacesMessages.error("Hata", e.getMessage());
            LOG.error("Search Save Error", e);
        }

    }

    public void onSavedSearchChanged() {
        if (savedSearch != null) {
            try {
                detailedSearchController.setSearchModel(savedSearchService.getSearchModel(savedSearch));
                for (String searchPanel : SearchRegistery.getSearchPanels()) {
                    SearchPanelController spc = BeanProvider.getContextualReference(searchPanel, false, SearchPanelController.class);
                    spc.changeEvent();
                }
                detailedSearchController.setSearchModel(savedSearchService.getSearchModel(savedSearch));

            } catch (IOException ex) {
                FacesMessages.error("Hata", ex.getMessage());
                LOG.error("Saved Search Read Error", ex);
            }
        }
    }

    public void removeSearch() {
        detailedSearchController.clearSearch();
        if(savedSearch != null){
            savedSearchService.removeSearchById(savedSearch);
        }
    }

    @PostConstruct
    public void init() {
        clearEvent();
        this.setOrder((short) 0);
    }

    @Override
    public String getTabFragment() {
        return "/search/generic.xhtml";
    }

    @Override
    public String getExternalFragments() {
        return "/search/genericExternal.xhtml";
    }

    @Override
    public void changeEvent() {
        // do nothing yet
    }

    @Override
    public void clearEvent() {
        detailedSearchController.getSearchModel().setSearchInDocumentName(Boolean.TRUE);
        detailedSearchController.getSearchModel().setSearchInDocumentTags(Boolean.FALSE);
        detailedSearchController.getSearchModel().setSearchInAllRafs(Boolean.FALSE);
        detailedSearchController.setExtendedColumnMap(new HashMap());
    }

    private String getCaseSensitiveFilterForGenericSearch(DetailedSearchModel searchModel, boolean isCaseSensetive) {
        StringBuilder caseSensitiveSearchFilterStringBuilder = new StringBuilder("(");
        List<String> fieldArray = new ArrayList<>();
        String caseSensetivequeryPattern = " nodes.%s LIKE '%%%s%%' ";
        String nonCaseSensetiveQueryPattern = " LOWER(nodes.%s) LIKE '%%%s%%' ";

        fieldArray.add("[jcr:name]");
        fieldArray.add("[jcr:title]");

        if (searchModel.getSearchInCreatedAndModifiedData()) {
            fieldArray.add("[jcr:lastModified]");
            fieldArray.add("[jcr:lastModifiedBy]");
            fieldArray.add("[jcr:created]");
            fieldArray.add("[jcr:createdBy]");
        }

        String searchText = isCaseSensetive ? escapeQueryParam(searchModel.getSearchText().trim()) : escapeQueryParam(searchModel.getSearchText().trim().toLowerCase(caseSensitiveSearchService.getSearchLocale())) ;
        String queryPattern = isCaseSensetive ? caseSensetivequeryPattern : nonCaseSensetiveQueryPattern;

        String indexString = fieldArray.stream()
                .map(fieldText -> String.format(queryPattern, fieldText, searchText))
                .collect(Collectors.joining(" OR "));

        caseSensitiveSearchFilterStringBuilder.append(indexString);

        caseSensitiveSearchFilterStringBuilder.append(")");
        return caseSensitiveSearchFilterStringBuilder.toString();
    }

    @Override
    public List getSearchQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel) {
        if ("JCR-SQL2".equals(queryLanguage)) {
            List<String> whereExpressions = new ArrayList();

            if (searchModel.getDateFrom() != null) {

                whereExpressions.add(String.format(" nodes.[%s] >= %s", PROP_CREATED_DATE, getJCRDate(searchModel.getDateFrom())));
            }

            if (searchModel.getDateTo() != null) {
                whereExpressions.add(String.format(" nodes.[%s] <= %s", PROP_CREATED_DATE, getJCRDate(searchModel.getDateTo())));
            }

            if (!Strings.isNullOrEmpty(searchModel.getSearchText())) {
                if (searchModel.getSearchInFileDataAvailable()) {
                    whereExpressions.add(String.format(" CONTAINS(nodes.[jcr:data], '\"%s\"')) ", escapeQueryParam(searchModel.getSearchText().trim())));
                } else if (searchModel.getSearchInDocumentTags()) {
                    if (searchModel.getCaseSensitive()) {
                        whereExpressions.add(String.format(" nodes.[raf:tags] LIKE '%%%1$s%%' ", escapeQueryParam(searchModel.getSearchText().trim())));
                    } else {
                        whereExpressions.add(String.format(" LOWER(nodes.[raf:tags]) LIKE '%%%1$s%%' ", escapeQueryParam(searchModel.getSearchText().trim().toLowerCase(caseSensitiveSearchService.getSearchLocale()))));
                    }
                }
                else {
                    if (searchModel.getSearchInDocumentName()) {
                        if (searchModel.getCaseSensitive()) {
                            whereExpressions.add(String.format(" nodes.[jcr:name] LIKE '%%%1$s%%' ", escapeQueryParam(searchModel.getSearchText().trim())));
                        } else {
                            whereExpressions.add(String.format(" LOWER(nodes.[jcr:name]) LIKE '%%%1$s%%' ", escapeQueryParam(searchModel.getSearchText().trim().toLowerCase(Locale.US))));
                        }
                    } else {
                        whereExpressions.add(getCaseSensitiveFilterForGenericSearch(searchModel, searchModel.getCaseSensitive()));
                    }
                }
            }

            List<String> searchSubPaths = new ArrayList<>();
            RafEncoder rafNameEncoder = RafEncoderFactory.getRafNameEncoder();

            if (!Strings.isNullOrEmpty(searchModel.getSearchSubPath()) && !searchModel.getSearchSubPath().equals("/RAF") && !searchModel.getSearchInAllRafs()) {
                searchSubPaths.add(searchModel.getSearchSubPath());
            } else {
                //Create where clauses for each raf definition and join them with OR
                rafs.forEach((RafDefinition raf) -> searchSubPaths.add(ApplicationContstants.RAF_ROOT + raf.getCode()));

                // Append private and shared rafs
                if (Boolean.TRUE.equals(identity.hasPermission("sharedRaf", "select")) && "true".equals(ConfigResolver.getPropertyValue("raf.shared.enabled", "true"))) {
                    searchSubPaths.add(rafNameEncoder.encode(ApplicationContstants.SHARED_RAF_ROOT));
                }
                if ("true".equals(ConfigResolver.getPropertyValue("raf.personal.enabled", "true"))) {
                    searchSubPaths.add(rafNameEncoder.encode(ApplicationContstants.PRIVATE_RAF_ROOT + identity.getLoginName()));
                }
            }

            whereExpressions.add(getSubPathSearchExpression(searchSubPaths));

            return whereExpressions;
        } else if ("elasticSearch".equals(queryLanguage)) {
            List mustQueryList = new ArrayList();
            if (!Strings.isNullOrEmpty(searchModel.getSearchSubPath())) {
                Map wildcard = new HashMap();
                Map filePath = new HashMap();
                filePath.put("filePath", searchModel.getSearchSubPath() + "*");
                wildcard.put("wildcard", filePath);
                mustQueryList.add(wildcard);
            } else {
                List<String> rafCodes = new ArrayList();
                rafs.forEach((r) -> {
                    rafCodes.add(r.getCode());
                });
                Map terms = new HashMap();
                Map rafCode = new HashMap();
                rafCode.put("rafCode", rafCodes);
                terms.put("terms", rafCode);
                mustQueryList.add(terms);
            }

            if (searchModel.getDateFrom() != null) {
                Map range = new HashMap();
                Map createDate = new HashMap();
                Map gte = new HashMap();
                gte.put("gte", searchModel.getDateFrom().getTime());
                createDate.put("createDate", gte);
                range.put("range", createDate);
                mustQueryList.add(range);
            }

            if (searchModel.getDateTo() != null) {
                Map range = new HashMap();
                Map createDate = new HashMap();
                Map lte = new HashMap();
                lte.put("lte", searchModel.getDateTo().getTime());
                createDate.put("createDate", lte);
                range.put("range", createDate);
                mustQueryList.add(range);
            }

            if (!Strings.isNullOrEmpty(searchModel.getSearchText())) {
                if (searchModel.getSearchInDocumentName()) {
                    Arrays.asList(searchModel.getSearchText().split(" ")).forEach((str) -> {
                        Map wildcard = new HashMap();
                        Map filePath = new HashMap();
                        filePath.put("title", "*" + str + "*");
                        wildcard.put("wildcard", filePath);
                        mustQueryList.add(wildcard);
                    });
                } else {
                    Arrays.asList(searchModel.getSearchText().split(" ")).forEach((str) -> {
                        Map wildcard = new HashMap();
                        Map filePath = new HashMap();
                        filePath.put("query", str);
                        wildcard.put("query_string", filePath);
                        mustQueryList.add(wildcard);
                    });
                }

                if (searchModel.getSearchInDocumentName()) {
                    Arrays.asList(searchModel.getSearchText().split(" ")).forEach((str) -> {
                        Map wildcard = new HashMap();
                        Map filePath = new HashMap();
                        filePath.put("tags", "*" + str + "*");
                        wildcard.put("wildcard", filePath);
                        mustQueryList.add(wildcard);
                    });
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

    @Override
    public List getSearchSortQuery(List<RafDefinition> rafs, String queryLanguage, DetailedSearchModel searchModel) {
        if (searchModel != null && !Strings.isNullOrEmpty(searchModel.getSortBy()) && !Strings.isNullOrEmpty(searchModel.getSortOrder())) {
            if ("JCR-SQL2".equals(queryLanguage)) {
                List<String> orderExpression = new ArrayList();
                Map<String, String> mapSort = new HashMap();
                mapSort.put("path", "nodes.[jcr:path]");
                mapSort.put("title", "nodes.[jcr:title]");
                mapSort.put("createBy", "nodes.[jcr:createBy]");
                mapSort.put("created", "nodes.[jcr:created]");
                mapSort.put("lastModifiedBy", "nodes.[jcr:lastModifiedBy]");
                mapSort.put("lastModified", "nodes.[jcr:lastModified]");
                if (mapSort.containsKey(searchModel.getSortBy())) {
                    orderExpression.add(mapSort.get(searchModel.getSortBy()).concat(" ").concat(searchModel.getSortOrder()));
                }
                return orderExpression;
            } else if ("elasticSearch".equals(queryLanguage)) {
                Map<String, String> mapSort = new HashMap();
                mapSort.put("path", "filePath");
                mapSort.put("title", "title");
                mapSort.put("createBy", "createBy");
                mapSort.put("created", "createDate");
                mapSort.put("lastModifiedBy", "updateBy");
                mapSort.put("lastModified", "updateDate");
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

    private String getSubPathSearchExpression(List<String> subPaths) {
        String expression = subPaths.stream()
                .distinct()
                .map(path -> String.format("(nodes.[jcr:path] LIKE '%s%%')", path))
                .collect(Collectors.joining(" OR "));

        return String.format("( %s )", expression);
    }
}
