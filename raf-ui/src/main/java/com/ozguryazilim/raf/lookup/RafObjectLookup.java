package com.ozguryazilim.raf.lookup;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.DialogPages;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractRafCollectionCompactViewController;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.feature.search.FeatureSearchResult;
import com.ozguryazilim.telve.lookup.Lookup;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
import com.ozguryazilim.telve.utils.ELUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Lookup(dialogPage = DialogPages.RafObjectLookup.class)
public class RafObjectLookup extends AbstractRafCollectionCompactViewController {

    private static final Logger LOG = LoggerFactory.getLogger(RafObjectLookup.class);

    private static final String SELECT_TYPE_DOCUMENT = "Document";
    private static final String SELECT_TYPE_FOLDER = "Folder";

    @Inject
    private ViewConfigResolver viewConfigResolver;

    @Inject
    private Identity identity;

    @Inject
    private RafService rafService;

    @Inject
    private RafDefinitionService rafDefinitionService;

    private String searchText;
    private String profile;
    private String listener;
    private Map<String, String> profileProperties;

    private RafObject selected;

    private List<RafDefinition> rafs;
    private RafDefinition selectedRaf;

    @PostConstruct
    public void init() {

    }

    /**
     * Geriye açılacak olan popup için view adı döndürür.
     *
     * Bu view dialogBase sınıfından türetilmiş olmalıdır.
     *
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
     * @param value mevcut veri. Ağaç tipi sınıflarda seçim için
     */
    public void openDialog(String profile, String listener, Object value) {

        this.profile = profile;
        this.listener = listener;

        parseProfile();
        initProfile();

        Map<String, Object> options = new HashMap<>();
        decorateDialog(options);

        clear();
        selected = null;

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
            setCollection(rafService.getCollectionPaged(getSelectedRaf().getNodeId(), 0, 100));
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
                setCollection(rafService.getCollectionPaged(object.getId(), 0, 100));
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

    public List<RafDefinition> getRafs() {
        if (rafs == null) {
            rafs = rafDefinitionService.getRafsForUser(identity.getLoginName(), true);
        }

        return rafs;
    }

    public RafDefinition getSelectedRaf() {
        if (selectedRaf == null) {
            selectedRaf = getRafs().get(0);
        }
        return selectedRaf;
    }

    public void setSelectedRaf(RafDefinition selectedRaf) {
        this.selectedRaf = selectedRaf;
        populateData();
    }

    public RafObject getSelected() {
        return selected;
    }

    public void goUpFolder() {
        //FIXME: Burada parent'ın null olması ihtimali, raf olması ihtimali kontrol edilecek.

        clear();
        try {
            setCollection(rafService.getCollectionPaged(getCollection().getParentId(), 0, 100));
        } catch (RafException ex) {
            LOG.error("Cannot find parent node", ex);
        }
    }

}
