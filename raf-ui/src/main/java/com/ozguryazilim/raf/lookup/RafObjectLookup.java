package com.ozguryazilim.raf.lookup;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.DialogPages;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractRafCollectionCompactViewController;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.feature.search.FeatureSearchResult;
import com.ozguryazilim.telve.lookup.Lookup;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
import com.ozguryazilim.telve.utils.ELUtils;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oyas
 */
@Lookup(dialogPage = DialogPages.RafObjectLookup.class)
public class RafObjectLookup extends AbstractRafCollectionCompactViewController {

    private static final Logger LOG = LoggerFactory.getLogger(RafObjectLookup.class);

    private static final String SELECT_TYPE_DOCUMENT = "Document";
    private static final String SELECT_TYPE_FOLDER = "Folder";

    @Inject
    @UserAware
    private Kahve kahve;

    private SortType sortBy;

    @Inject
    private ViewConfigResolver viewConfigResolver;

    @Inject
    private RafService rafService;

    @Inject
    private Identity identity;

    private String searchText;
    private String profile;
    private String listener;
    private Map<String, String> profileProperties;

    private RafObject selected;

    private int page = 0;
    private int pageSize = 50;

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public SortType getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortType sortBy) {
        this.sortBy = sortBy;
    }

    @PostConstruct
    public void init() {
        setSortBy(SortType.defaultSortType(kahve.get("raf.sortBy", "DATE_DESC").getAsString()));
    }

    /**
     * Geriye açılacak olan popup için view adı döndürür.
     *
     * Bu view dialogBase sınıfından türetilmiş olmalıdır.
     *
     * @return
     */
    public String getDialogName() {
        String viewId = getDialogPageViewId();
        return viewId.substring(0, viewId.indexOf(".xhtml"));
    }

    /**
     * Dialog için sınıf annotationı üzerinden aldığı Page ID'sini döndürür.
     *
     * @return
     */
    public String getDialogPageViewId() {
        return viewConfigResolver.getViewConfigDescriptor(getDialogPage()).getViewId();
    }

    /**
     * Sınıf işaretçisinden @Lookup page bilgisini alır
     *
     * @return
     */
    public Class<? extends ViewConfig> getDialogPage() {
        return ((Lookup) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(Lookup.class)).dialogPage();
    }

    /**
     * Gelen profile stringine göre bir şey yapılacaksa alt sınıflar tarafından
     * override edilir.
     */
    public void initProfile() {
        //Override edilmek üzere içi boş.
        //FIXME: Folder/Document seçimi için profil alınacak
    }

    /**
     * Geriye Dialoğ için seçim tipini döndürür.
     *
     * Folder ya da Document olabilir default Document
     *
     * @return
     */
    protected String getSelectionType() {
        return profileProperties.getOrDefault("selectionType", SELECT_TYPE_DOCUMENT);
    }

    /**
     * İlgili sınıfa ait dialogu açar
     *
     * @param profile sorgu profili
     * @param listener sonuçlar nereye gidecek?
     * @param subPath mevcut veri. Ağaç tipi sınıflarda seçim için
     */
    public void openDialog(String profile, String listener, String subPath) {
        page = 0;
        this.profile = profile;
        this.listener = listener;

        parseProfile();
        initProfile();

        Map<String, Object> options = new HashMap<>();
        decorateDialog(options);

        try {
            RafEncoder encoder = RafEncoderFactory.getRafNameEncoder();
            String encodedUserName = encoder.encode(identity.getLoginName());
            if (subPath == null || subPath.equals("") ||
                    (subPath.startsWith("/PRIVATE") && !subPath.equals("/PRIVATE/" + encodedUserName))) {
                selected = null;
            } else {
                selected = rafService.getRafObjectByPath(subPath);
            }
        } catch (RafException ex) {
            LOG.error("Raf Sub Path Cannot Handle.", ex);
        }

        clear();

        if (autoSearch()) {
            search();
        }

        RequestContext.getCurrentInstance().openDialog(getDialogName(), options, null);
    }

    /**
     * Açılacak olan diolog özellikleri setlenir.
     *
     * Alt sınıflar isterse bu methodu override ederk dialoğ özellikleirni
     * değiştirebilirler.
     *
     * @param options
     */
    protected void decorateDialog(Map<String, Object> options) {
        options.put("modal", true);
        //options.put("draggable", false);
        options.put("resizable", false);
        options.put("contentHeight", 450);
    }

    /**
     * Popup açıldığında otomatik arama yapmaması için override edilmeli.
     *
     * @return
     */
    protected boolean autoSearch() {
        return true;
    }

    /**
     * Seçim bilgisi ile birlikte dialoğu kapatır.
     *
     * Eğer bir den fazla seçilen değer varsa bunları List olarak, tek seçim
     * varsa Object olarak seçim yoksa null değer olarak döndürür.
     *
     * İlgili seçimle ilgilenen sınıf bu bilgileri şu şekilde alabilir :
     *
     * <code>
     * public void onCarChosen(SelectEvent event) {
     *       Car car = (Car) event.getObject();
     *       ......
     *   }
     * </code>
     *
     *
     */
    public void closeDialog() {

        LookupSelectTuple sl = getLookupSelectTuple();

        //Eğer bir şey seçilmemiş ise sadece dialoğu kapatalım.
        if (sl == null) {
            RequestContext.getCurrentInstance().closeDialog(null);
            return;
        }

        //eğer bir event listener var ise
        if (listener.contains("event:")) {
            triggerListeners(listener, sl.getValue());
        }
        //Buraya listede gelebilir
        //lookupSelectEvent.fire((E)sl.getValue());

        RequestContext.getCurrentInstance().closeDialog(sl);

    }

    /**
     * Dialog geri dönüşlerinin varsayılan dnleyicisi.
     *
     * Select tuple üzerinde bulunan bilgiyi kullanarak EL ile ilgili yere atama
     * yapar.
     *
     * @param event
     */
    public void onSelect(SelectEvent event) {
        if (event.getObject() instanceof FeatureSearchResult) {
            String expression = "";
            if (!Strings.isNullOrEmpty(listener) && !listener.contains(":")) {
                expression = "#{" + listener + "}";
            }
            if (Strings.isNullOrEmpty(expression)) {
                return;
            }

            //EL üzerinden değeri yazacağız
            ELUtils.setObject(expression, event.getObject());
            return;
        }

        LookupSelectTuple sl = (LookupSelectTuple) event.getObject();
        if (sl == null) {
            return;
        }

        if (sl.getExpression().isEmpty()) {
            return;
        }

        //EL üzerinden değeri yazacağız
        ELUtils.setObject(sl.getExpression(), sl.getValue());

    }

    /**
     * GUI'den seçilen bilgileri kullanarak seçim paketi oluşturur.
     *
     * @return
     */
    protected LookupSelectTuple getLookupSelectTuple() {
        LookupSelectTuple sl = null;

        String expression = "";
        if (!Strings.isNullOrEmpty(listener) && !listener.contains(":")) {
            expression = "#{" + listener + "}";
        }

        if (selected != null) {
            sl = new LookupSelectTuple(expression, selected);
        }

        return sl;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Seçim sonrası listener'la mesaj gönderilir.
     *
     * @param o
     */
    protected void triggerListeners(String event, Object o) {
        /*
        List<LookupSelectListener> ls = listeners.get(event);
        if( ls != null ){
            for( LookupSelectListener l : ls ){
                l.onSelect(o);
            }
        }
         */
    }

    /**
     * Dialogu hiç bir şey seçmeden kapatır.
     */
    public void cancelDialog() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    /**
     * Veri sorgulaması yapılıp model nesnesi doldurulmalıdır.
     */
    public void populateData() {
        //FIXME: asıl değişecek yer burası! Lookup için özel bir ağaç modeli hazırlamak lazım sanırım.
        //Ağaç formunda hem raf/folder/document şeklinde liste vermeli!

        Map<String, Object> params = new HashMap<>();

        try {
            /*
            //Şimdi de toplam folderları bir toparlayalım
            List<RafFolder> folders = new ArrayList<>();
            for( RafDefinition raf : rafs ){
            try {
            folders.addAll( rafService.getFolderList(raf.getNode().getPath()));
            } catch (RafException ex) {
            LOG.error( "Raf Folders cannot populate", ex);
            }
            }
             */

            //LOG.debug("Populated Folders : {}", folders);
            clear();
            setCollection(rafService.getCollectionPaged(getSelected().getId(), getPage(), getPageSize(), SELECT_TYPE_FOLDER.equals(getSelectionType()), getSortBy(), false));
        } catch (RafException ex) {
            LOG.error("Raf Folders cannot populate", ex);
        }

    }

    /**
     * GUI'den yeni arama talebi karşılar.
     */
    public void search() {

        /* FIXME: result nasıl olacak ise burası ondan sonra düzenlenecek!
        if ( !results.isEmpty() ) {
            results.clear();
        }
         */
        populateData();

    }

    /**
     * Profile stringlerini parse edip properties olarak model sınıflarına
     * koyar.
     *
     * Profile string formatı :
     *
     * key1:value1;key2:value2 şeklinde olur.
     *
     */
    protected void parseProfile() {

        if (!Strings.isNullOrEmpty(profile)) {
            profileProperties = Splitter.on(';').omitEmptyStrings().trimResults().withKeyValueSeparator(':').split(profile);
        } else {
            profileProperties = new HashMap<>();
        }

    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getCaptionFieldName() {
        return "";
    }

    public boolean getSupportBreadcrumb() {
        return false;
    }

    public boolean isSelected(RafObject object) {
        return false;
    }

    public void toggle(RafObject object) {

    }

    public void selectItem(RafObject object) {
        LOG.debug("Selection : {}", object);
        try {
            if (object instanceof RafFolder) {
                clear();
                setCollection(rafService.getCollectionPaged(object.getId(), getPage(), getPageSize(), SELECT_TYPE_FOLDER.equals(getSelectionType()), getSortBy(), false));
                if (SELECT_TYPE_FOLDER.equals(getSelectionType())) {
                    selected = object;
                }
            } else {
                if (SELECT_TYPE_DOCUMENT.equals(getSelectionType())) {
                    selected = object;
                }
            }
        } catch (RafException ex) {
            LOG.error("Raf Object seçilemedi", ex);
        }
    }

    public void selectItem(String path) {
        try {
            RafObject object = rafService.getRafObjectByPath(path);
            selectItem(object);
        } catch (RafException ex) {
            LOG.error("Raf Object seçilemedi", ex);
        }
    }

    public RafObject getSelected() {
        try {
            if (this.selected == null) {
                this.selected = rafService.getRafObjectByPath("/RAF/");
            }
        } catch (RafException ex) {
            LOG.error("Cannot find node from given path", ex);
        }
        return selected;
    }

    public void goUpFolder() {
        if (!hasParent()) {
            return;
        }
        clear();
        try {
            page = 0;
            setCollection(rafService.getCollectionPaged(getCollection().getParentId(), getPage(), getPageSize(), SELECT_TYPE_FOLDER.equals(getSelectionType()), getSortBy(), false));
            selectItem(getCollection().getPath());
        } catch (RafException ex) {
            LOG.error("Cannot find parent node", ex);
        }
    }

    public boolean hasParent() {
        return getCollection().getPath().startsWith("/RAF/") && getCollection().getParentId() != null && !getCollection().getParentId().equals("/");
    }

    public Long getTotalFileCount() throws RafException {
        return rafService.getChildCount(getCollection().getPath());
    }

}
