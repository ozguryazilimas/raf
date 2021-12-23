package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.events.RafObjectDeleteEvent;
import com.ozguryazilim.raf.events.RafUploadEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.AbstractSidePanel;
import com.ozguryazilim.raf.ui.base.ActionRegistery;
import com.ozguryazilim.raf.ui.base.ContentPanelRegistery;
import com.ozguryazilim.raf.ui.base.ContentViewPanel;
import com.ozguryazilim.raf.ui.base.ObjectContentViewPanel;
import com.ozguryazilim.raf.ui.base.SidePanelRegistery;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.view.Pages;
import org.apache.commons.io.FileUtils;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Temel Raf arayüzü controller sınıfı.
 *
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class RafController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafController.class);

    @Inject
    private Identity identity;

    @Inject
    @UserAware
    private Kahve kahve;

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private RafService rafService;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafObjectMemberService;

    @Inject
    private RafContext context;

    @Inject
    private Event<RafChangedEvent> rafChangedEvent;

    @Inject
    private Event<RafCollectionChangeEvent> rafCollectionChangeEvent;

    @Inject
    private Event<RafFolderChangeEvent> folderChangedEvent;

    private AbstractSidePanel selectedSidePanel;

    private ContentViewPanel selectedContentPanel;
    private ContentViewPanel selectedCollectionContentPanel;

    @Inject
    private Instance<ObjectContentViewPanel> objectViewPanels;

    private String rafCode;

    private String objectId;

    private RafDefinition rafDefinition;

    private Boolean showFolders = Boolean.TRUE;
    private Boolean showSidePanel = Boolean.TRUE;
    private Boolean showManagerTools = Boolean.TRUE;
    private Boolean showRafObjectManagerTools = Boolean.TRUE;

    private Integer page = 0;
    private Integer pageSize = 200;
    private Integer pageCount = 0;

    private SortType sortBy = SortType.DATE_DESC;
    private Boolean descSort = Boolean.FALSE;

    private String lastRafObjectId;

    public Boolean getDescSort() {
        return descSort;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public SortType getSortBy() {
        return sortBy;
    }

    @PostConstruct
    public void initDefaults() {
        showFolders = kahve.get("raf.showFolders", Boolean.TRUE).getAsBoolean();
        showSidePanel = kahve.get("raf.showSidePanel", Boolean.TRUE).getAsBoolean();

        //Kahveden sınıf ismine bakıp doğrusunu seçelim.
        String colViewName = kahve.get("raf.collectionView", "RafCollectionCompactViewPanel").getAsString();
        colViewName = decapitalize(colViewName);
        selectedContentPanel = BeanProvider.getContextualReference(colViewName, true, ContentViewPanel.class);
        selectedCollectionContentPanel = selectedContentPanel;
        //selectedContentPanel= collectionCompactViewPanel;
        //selectedCollectionContentPanel = collectionCompactViewPanel;
        setPage(0);
        setSortBy(SortType.defaultSortType(kahve.get("raf.sortBy", "DATE_DESC").getAsString()));
        setDescSort(kahve.get("raf.descSort", Boolean.FALSE).getAsBoolean());
    }

    public void nextPage() {
        if (context.getCollection() != null && context.getCollection().getItems() != null && !context.getCollection().getItems().isEmpty()) {
            setPage(getPage() + getPageSize());
        }
    }

    public void previousPage() {
        int newPage = getPage() - getPageSize();
        if (newPage < 0) {
            newPage = 0;
        }
        setPage(newPage);
    }

    /**
     *
     * FIXME: bu davranış bir Util sınıfa gitmeli. Telve seviyesinde bir
     * yerlerde olmalı. Guava'dan da kurtulmak için lazım.
     *
     * Copied from
     * https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
     *
     * @param string
     * @return
     */
    private static String decapitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        char c[] = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    /**
     * Sayfa çağrıldığında init olması için çağrılır.
     *
     * ViewAction olarak
     */
    public void init() {
        setPage(0);

        //FIXME: Bu fonksiyon parçalanıp düzenlenmeli.
        if (Strings.isNullOrEmpty(rafCode) && !Strings.isNullOrEmpty(objectId)) {
            try {
                //Eğer rafCode gelmemiş ama objectId gelmiş ise Raf'ı object üzerinden bulalım
                RafObject obj = rafService.getRafObject(objectId);
                String[] ss = obj.getPath().split("/");
                // /RAF/hede/xxx geriye ilki boş 3 item dönüyor
                if ("PRIVATE".equals(ss[1])) {
                    rafCode = "PRIVATE";
                } else if ("SHARED".equals(ss[1])) {
                    rafCode = "SHARED";
                } else if ("RAF".equals(ss[1])) {
                    rafCode = ss[2];
                } else {
                    //Buraya düştü ise PROCESS ya da saçma bişi olacaktır dolayısı ile
                    //Buraya normal arayüzle girme yetkisi yok
                    viewNavigationHandler.navigateTo(Pages.Home.class);
                    return;
                }

            } catch (RafException ex) {
                LOG.error("Raf Exception", ex);
            }

        } else if (Strings.isNullOrEmpty(rafCode)) {
            //Eğer bir şey atanmamış ise kişisel raf olsun.
            rafCode = "PRIVATE";
        }

        try {
            rafDefinition = rafDefinitionService.getRafDefinitionByCode(rafCode);
        } catch (RafException ex) {
            //FIXME: Burada ne yapmalı?
            LOG.error("Error", ex);
            viewNavigationHandler.navigateTo(Pages.Home.class);
            return;
        }

        try {
            //Uye değilse hemen HomePage'e geri gönderelim.
            if (!memberService.isMemberOf(identity.getLoginName(), rafDefinition)) {
                viewNavigationHandler.navigateTo(Pages.Home.class);
                return;
            }
        } catch (RafException ex) {
            LOG.error("Error", ex);
            //Gene de geldiği yere gönderelim.
            viewNavigationHandler.navigateTo(Pages.Home.class);
            return;
        }

        try {
            showManagerTools = rafDefinition.getId() > 0 && memberService.hasManagerRole(identity.getLoginName(), rafDefinition);
        } catch (RafException ex) {
            showManagerTools = Boolean.FALSE;
        }

        //FIXME: burada aslında hala bir hata durumu var. parametre olarak alınan RAF'a erişim yetkisi olmayabilir. Ya da öyle bir raf gerçekten olmayabilir.
        context.setSelectedRaf(rafDefinition);

        if (!Strings.isNullOrEmpty(objectId)) {

            try {
                //Demek ki istenilen bir nesne var. Önce onu bir bulalım.
                RafObject obj = rafService.getRafObject(objectId);

                RafFolder fld = null;
                //Selected object panel seçininden önce yapılmalı, zira panel object tipine göre tespit ediliyor.
                context.setSelectedObject(obj);
                //şimdi objenin tipine bakarak bazı kararlar verelim
                if (obj instanceof RafDocument || obj instanceof RafRecord) {

                    //Folder'ı bir bulalım
                    //TODO: tip kontrolü yapmaya gerek var mı?
                    fld = (RafFolder) rafService.getRafObject(obj.getParentId());
                    //FIXME: Doğru paneli nasıl seçeceğiz?
                    selectedContentPanel = getObjectContentPanel();

                } else if (obj instanceof RafFolder) {
                    fld = (RafFolder) obj;
                    //FIXME: burada kullanıcı tercihlerinden alınması gerekir.
                    //FIXME: DOğru paneli nasıl seçeceğiz?
                    selectedContentPanel = getCollectionContentPanel();
                }

                populateFolderCollection(fld.getId());

                try {
                    showRafObjectManagerTools = !Strings.isNullOrEmpty(getObjectId()) && rafDefinition.getId() > 0 && (memberService.hasManagerRole(identity.getLoginName(), rafDefinition) || rafObjectMemberService.hasManagerRole(identity.getLoginName(), obj.getPath()));
                } catch (RafException ex) {
                    showRafObjectManagerTools = Boolean.FALSE;
                }

            } catch (RafException ex) {
                FacesMessages.error("Document Selection Error", ex.getLocalizedMessage());
                LOG.error("Raf Exception", ex);
            }

        } else {
            //Demek permalink olarak istenen bişi yok. O zaman rootu alalım.
            //FIXME: burda exception handling gerekli
            try {
                populateFolderCollection(rafDefinition.getNodeId());
            } catch (RafException ex) {
                LOG.error("Raf Exception", ex);
            }

            //FIXME: bundan çok emin değilim. RafFolder değil RafNode bağladık çünkü.
            context.setSelectedObject(rafDefinition.getNode());
            selectedContentPanel = getCollectionContentPanel();
        }

        try {
            context.setFolders(rafService.getRootFolders(context.getSelectedRaf().getNode().getPath()));
        } catch (RafException ex) {
            //FIXME: ne yapacağız?
            LOG.error("Raf Exception", ex);
        }

        rafChangedEvent.fire(new RafChangedEvent());
    }

    public String getRafCode() {
        return rafCode;
    }

//    public Date getRafObjectCreateDateOrUpdateDate(RafObject rafObject) {
//        if (rafObject.getUpdateDate() != null) {
//            return rafObject.getUpdateDate();
//        } else {
//            return rafObject.getCreateDate();
//        }
//    }
//
//    public String getRafObjectCreatorOrUpdater(RafObject rafObject) {
//        if (rafObject.getUpdateBy() != null) {
//            return rafObject.getUpdateBy();
//        } else {
//            return rafObject.getCreateBy();
//        }
//    }
    public void setDescSort(Boolean descSort) {
        setSortByAndType(this.sortBy, descSort);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public RafDefinition getRafDefinition() {
        return rafDefinition;
    }

    public void setRafDefinition(RafDefinition rafDefinition) {
        this.rafDefinition = rafDefinition;
    }

    /**
     * Geriye SidePanel listesini döndürür.
     *
     * @return
     */
    public List<AbstractSidePanel> getSidePanels() {
        return SidePanelRegistery.getSidePanels();
    }

    public AbstractSidePanel getSelectedSidePanel() {
        //Eğer seçili bir yoksa ilkini seçiyoruz.
        if (selectedSidePanel == null) {
            selectedSidePanel = getSidePanels().get(0);
        }
        return selectedSidePanel;
    }

    public void setSelectedSidePanel(AbstractSidePanel selectedSidePanel) {
        this.selectedSidePanel = selectedSidePanel;
    }

    public List<ContentViewPanel> getContentPanels() {
        return ContentPanelRegistery.getPanels();
    }

    public List<ContentViewPanel> getCollectionContentPanels() {
        return getContentPanels().stream()
                .filter(p -> p.getSupportCollection())
                .collect(Collectors.toList());
    }

    public ContentViewPanel getSelectedContentPanel() {
        //Eğer seçili bir yoksa ilkini seçiyoruz.
        if (selectedContentPanel == null) {
            selectedContentPanel = getContentPanels().get(0);
        }
        return selectedContentPanel;
    }

    public void setSelectedContentPanel(ContentViewPanel selectedContentPanel) {
        this.selectedContentPanel = selectedContentPanel;
        if (selectedContentPanel.getSupportCollection()) {
            this.selectedCollectionContentPanel = selectedContentPanel;
            LOG.debug("Selected Collection View : {}", selectedContentPanel.getName());
            kahve.put("raf.collectionView", selectedContentPanel.getName());
        }
    }

    /**
     * FIXME: Kullanıcı seçimlerini dikkate almak gerek. Şu anda ilk bulduğunu
     * dönüyor.
     *
     * @return
     */
    protected ContentViewPanel getObjectContentPanel() {

        //FIXME: burada instance sonuçları cachelenmeli aslında. Sistemde sornadna gelen bişiler yok. Ve burası çok sık çağrılıyor
        Iterator<ObjectContentViewPanel> it = objectViewPanels.iterator();
        while (it.hasNext()) {
            ObjectContentViewPanel p = it.next();
            if (p.acceptObject(context.getSelectedObject())) {
                p.setRafObject(context.getSelectedObject());
                return p;
            }
        }

        /*
        //FIXME: burada previeww panelleri gibi aslında mimeType'a / nodeType'a bakarak doğru paneli seçmek lazım
        if (context.getSelectedObject() instanceof RafDocument) {
            documentViewPanel.setObject((RafDocument)context.getSelectedObject());
            return documentViewPanel;
        } else if (context.getSelectedObject() instanceof RafFolder) {
            folderViewPanel.setObject((RafFolder)context.getSelectedObject());
            return folderViewPanel;
        } else if (context.getSelectedObject() instanceof RafRecord) {
            recordViewPanel.setObject((RafRecord)context.getSelectedObject());
            return recordViewPanel;
        }
         */
        //FIXME: Hiçibişi seçilmemesi durumu riski var.
        LOG.warn("RafObjectViewPanel not found for Object Type '{}'", context.getSelectedObject());
        return null;

    }

    /**
     * FIXME: Kullanıcı seçimlerini dikkate almak gerek. Şu anda ilk bulduğunu
     * dönüyor.
     *
     * @return
     */
    protected ContentViewPanel getCollectionContentPanel() {

        if (selectedCollectionContentPanel == null) {
            for (ContentViewPanel p : getContentPanels()) {
                if (p.getSupportCollection()) {
                    selectedCollectionContentPanel = p;
                    return p;
                }
            }
        } else {
            return selectedCollectionContentPanel;
        }

        return selectedCollectionContentPanel;
    }

    public void selectItem(RafObject item) {

        //FIXME: exception handling
        //tipe bakarak tek bir RafObject mi yoksa collection mı olacak seçmek lazım. Dolayısı ile hangi view seçeleceği de belirlenmiş olacak.
        if (item instanceof RafFolder) {
            selectFolderById(item.getId());
        } else if (item instanceof RafDocument) {
            selectDocument((RafDocument) item);
        } else if (item instanceof RafRecord) {
            selectRecord((RafRecord) item);
        }

    }

    /**
     * Folder'ın detayları için detail view isteniyor demek.
     *
     * Aslında folder edit edilecek.
     *
     * @param item
     */
    public void selectFolderItem(RafObject item) {

        //FIXME: exception handling
        //tipe bakarak tek bir RafObject mi yoksa collection mı olacak seçmek lazım. Dolayısı ile hangi view seçeleceği de belirlenmiş olacak.
        if (item instanceof RafFolder) {
            context.setSelectedObject(item);
            //FIXME: Burayı nasıl düzenlesek acaba? İçerik sunumu için aslında doğru paneli nasıl şeçeceğiz?
            selectedContentPanel = getObjectContentPanel();
            //selectFolderById(item.getId());
        } else if (item instanceof RafDocument) {
            selectDocument((RafDocument) item);
        }

    }

    public void selectDocument(RafDocument item) {
        context.setSelectedObject(item);
        context.getSeletedItems().clear();
        context.getSeletedItems().add(item);
        //FIXME: Burayı nasıl düzenlesek acaba? İçerik sunumu için aslında doğru paneli nasıl şeçeceğiz?
        selectedContentPanel = getObjectContentPanel();
    }

    public void selectRecord(RafRecord item) {
        context.setSelectedObject(item);
        context.getSeletedItems().clear();
        context.getSeletedItems().add(item);
        //FIXME: Burayı nasıl düzenlesek acaba? İçerik sunumu için aslında doğru paneli nasıl şeçeceğiz?
        selectedContentPanel = getObjectContentPanel();
    }

    public void selectFolderById(String folderId) {

        context.setSelectedObject(findFolder(folderId));
        folderChangedEvent.fire(new RafFolderChangeEvent());

        //FIXME: Doğru paneli nasıl seçeceğiz?
        selectedContentPanel = getCollectionContentPanel();
    }

    public void closeObjectPanel() {
        //FIXME: bundan pek emin değilim. SelectedObject'e null mu atamalı?
        context.setSelectedObject(null);
        //ObjectPanel'e geçerken obje aynı zamanda seçiliyor kapatırken de silelim
        context.getSeletedItems().clear();
        selectedContentPanel = getCollectionContentPanel();
    }

    /**
     * Context'e bulunan RafFolder içinden idsi verilen folder'ı bulur. Bulamaz
     * ise null döner.
     *
     * @param id
     * @return
     */
    private RafFolder findFolder(String id) {

        try {
            /* FIXME: Folder listesi artık context üzerinde bu şekilde tutulmuyor.
            for (RafFolder f : context.getFolders()) {
            if (id.equals(f.getId())) {
            return f;
            }
            }
            
            return null;
             */

            // Burada Bulunan RafFolder'ı context folder listesine ekleyelim. En azından bulunanlar chache girmiş olur.
            RafFolder res = (RafFolder) rafService.getRafObject(id);
            context.getFolders().add(res);

            return res;
        } catch (RafException ex) {
            LOG.error("Folder not found", ex);
            return null;
        }

    }

    public void selectNode() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String nodeId = params.get("nodeId");
        if (!Strings.isNullOrEmpty(nodeId)) {
            selectFolderById(nodeId);
        }
    }

    public void selectCategory() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String nodeId = params.get("nodeId");
        if (!Strings.isNullOrEmpty(nodeId)) {
            //selectFolderById(nodeId);
            //TODO: Aslında burda id değil doğrudan kategori kodu lazım bize. Sonuçta sorgu çekeceğiz.
            LOG.info("Selected Category ID : {}", nodeId);
            try {
                Long catId = Long.parseLong(nodeId);
                populateCategoryCollection(catId);
            } catch (RafException ex) {
                //FIXME: i18n ve gerçekten ne yapılmalı?
                LOG.error("Category Selection Error", ex);
                FacesMessages.error("Category Selection Error", ex.getLocalizedMessage());
            }
        }
    }

    public void selectTag(String tag) {
        LOG.info("Selected Tag : {}", tag);
        try {
            populateTagCollection(tag);
        } catch (RafException ex) {
            //FIXME: i18n ve gerçekten ne yapılmalı?
            LOG.error("Tag Selection Error", ex);
            FacesMessages.error("Tag Selection Error", ex.getLocalizedMessage());
        }
    }

    /**
     * Geriye uygulanabilir durumdaki action'ları döndürür.
     *
     * TODO: Burada action listesinin hazırlanması ile ilgili bir cache
     * mekanizması düşünmek lazım. Her seferinde hesaplamak biraz sorunlu.
     *
     * @return
     */
    public List<List<AbstractAction>> getActions() {
        //FIXME: Yetki kontrolü

        List<List<AbstractAction>> result = new ArrayList<>();

        List<AbstractAction> acts = ActionRegistery.getActions();
        Map< Integer, List<AbstractAction>> actGroups = acts.stream()
                .filter(a -> a.applicable(selectedContentPanel.getSupportCollection()))
                .sorted(new Comparator<AbstractAction>() {
                    @Override
                    public int compare(AbstractAction a1, AbstractAction a2) {
                        return a1.getOrder() < a2.getOrder() ? -1 : a1.getOrder() > a2.getOrder() ? 1 : 0;
                    }
                })
                .collect(Collectors.groupingBy(AbstractAction::getGroup, Collectors.toList()));

        for (Integer g : actGroups.keySet()) {
            result.add(actGroups.get(g));
        }

        return result;
    }

    /**
     * Nesne silinmesini dinler ve context'i düzenler.
     *
     * @param event
     */
    public void deleteListener(@Observes RafObjectDeleteEvent event) {
        LOG.debug("RafObjectDeleteEvent");
        //Ne olursa olsun. Bişi silinmiş ise selectedObject kalmamıştır
        context.setSelectedObject(null);
        //Silinen nesneyi context collection'ınından çıkaralım
        context.getCollection().getItems().remove(event.getPayload());
        //Seçili olan panel'i düzeltelim ve collection panele geçelim
        selectedContentPanel = getCollectionContentPanel();

        rafCollectionChangeEvent.fire(new RafCollectionChangeEvent());
    }

    public void setSortBy(SortType sortBy) {
        setSortByAndType(sortBy, this.descSort);
    }

    public void setSortByAndType(SortType sortBy, Boolean descSort) {
        boolean changing = !this.sortBy.equals(sortBy) || this.descSort != descSort;
        if (changing) {
            setPage(0);
            this.sortBy = sortBy;
            this.descSort = descSort;
            kahve.put("raf.sortBy", sortBy.name());
            kahve.put("raf.descSort", descSort);
            try {
                if (context.getSelectedObject() != null) {
                    populateFolderCollection(context.getSelectedObject().getId());
                } else if (context.getCollection() != null) {
                    populateFolderCollection(context.getCollection().getId());
                }

            } catch (RafException ex) {
                LOG.error("Raf Exception", ex);
            }
        }
    }

    /**
     * Birşeyler upload edildiğinde çağırılır.
     *
     * @param event
     */
    public void uploadListener(@Observes RafUploadEvent event) {
        LOG.debug("RafUploadEvent");
        //Collection'ı yeniden çekmek lazım.
        selectFolderById(context.getCollection().getId());

    }

    public void folderChangeListener(@Observes RafFolderChangeEvent event) {
        setPage(0);
        //FIXME: exception handling
        //FIXME: tipe bakarak tek bir RafObject mi yoksa collection mı olacak seçmek lazım. Dolayısı ile hangi view seçeleceği de belirlenmiş olacak.
        try {
            if (context.getSelectedObject() != null) {
                populateFolderCollection(context.getSelectedObject().getId());
            } else {
                populateFolderCollection(context.getCollection().getId());
            }

        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
    }

    /**
     * Birşeyler upload edildiğinde çağırılır.
     *
     * @param event
     */
    public void folderDataListener(@Observes RafFolderDataChangeEvent event) {
        LOG.info("RafFolderCreateEvent");
        try {
            if (context.getSelectedObject() != null) {
                populateFolderCollection(context.getSelectedObject().getId());
            } else {
                populateFolderCollection(context.getCollection().getId());
            }

        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }

        //Collection'ı yeniden çekmek lazım.
        //selectFolderById(context.getCollection().getId());

        /* FIXME: Folder yükleme yapısında değişiklik yapılıyor. Daha kontrollü bir şekilde düzeltilmeli.
        try {
            context.setFolders(rafService.getFolderList(context.getSelectedRaf().getNode().getPath()));
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
         */
    }

    protected void populateFolderCollection(String folderId) throws RafException {
        RafCollection collection = rafService.getCollectionPaged(folderId, getPage(), getPageSize(), false, getSortBy(), getDescSort());

        if (collection == null || collection.getItems() == null) {
            LOG.warn("[RAF-0006] Raf collection not found. folderID: {}", folderId);
            return;
        }

        if (!showFolders) {
            //Eğer UI'da folder görülmesin isteniyor ise filtreliyoruz.
            collection.setItems(
                    collection.getItems()
                            .stream()
                            .filter(o -> !"raf/folder".equals(o.getMimeType()))
                            .collect(Collectors.toList())
            );
        }

        if (context.getCollection() == null || !context.getCollection().getId().equals(folderId)) {
            //farklı bir klasör içeriği lissteleniyor veya klasörün içeriği ilk defa listeleniyor.
            context.setCollection(collection);
        } else {
            //mevcut listedeki son elemanın id si ile yeni listedeki son eleman farklı ise yeni sayfa verisi eklenmeli
            //yeni dosya eklenmiş ise de context'i güncelliyoruz.
            if (!collection.getItems().isEmpty() && (!lastRafObjectId.equals(collection.getItems().get(collection.getItems().size() - 1).getId())
                    || collection.getItems().size() > context.getCollection().getItems().size())) {
                for (RafObject item : collection.getItems()) {
                    if (item != null && !context.getCollection().getItems().contains(item)) {
                        context.getCollection().getItems().add(item);
                    }
                }
            }

        }
        lastRafObjectId = collection.getItems().isEmpty() ? "" : collection.getItems().get(collection.getItems().size() - 1).getId();
        rafCollectionChangeEvent.fire(new RafCollectionChangeEvent());
    }

    protected void populateCategoryCollection(Long categoryId) throws RafException {
        RafCollection collection = rafService.getCategoryCollectionById(categoryId, context.getSelectedRaf().getNode().getPath());

        if (!showFolders) {
            //Eğer UI'da folder görülmesin isteniyor ise filtreliyoruz.
            collection.setItems(
                    collection.getItems()
                            .stream()
                            .filter(o -> !"raf/folder".equals(o.getMimeType()))
                            .collect(Collectors.toList())
            );
        }

        context.setCollection(collection);

        rafCollectionChangeEvent.fire(new RafCollectionChangeEvent());
    }

    protected void populateTagCollection(String tag) throws RafException {
        RafCollection collection = rafService.getTagCollection(tag, context.getSelectedRaf().getNode().getPath());

        if (!showFolders) {
            //Eğer UI'da folder görülmesin isteniyor ise filtreliyoruz.
            collection.setItems(
                    collection.getItems()
                            .stream()
                            .filter(o -> !"raf/folder".equals(o.getMimeType()))
                            .collect(Collectors.toList())
            );
        }

        context.setCollection(collection);

        rafCollectionChangeEvent.fire(new RafCollectionChangeEvent());
    }

    /* FIXME: şimdilik arayüzde sadece category id yeterli gibi duruyor.
    protected void populateCategoryCollection( String category) throws RafException{
        
        RafCollection collection = rafService.getCategoryCollection(category, context.getSelectedRaf().getNode().getPath());
        
        if( !showFolders ){
            //Eğer UI'da folder görülmesin isteniyor ise filtreliyoruz.
            collection.setItems(
                collection.getItems()
                        .stream()
                        .filter( o -> !"raf/folder".equals(o.getMimeType()))
                        .collect(Collectors.toList())
            );
        }
        
        context.setCollection(collection);
    }
     */
    public void toggleShowFolders() {
        showFolders = !showFolders;

        kahve.put("raf.showFolders", showFolders);

        folderChangedEvent.fire(new RafFolderChangeEvent());
    }

    public Boolean getShowFolders() {
        return showFolders;
    }

    public void toggleSidePanel() {
        showSidePanel = !showSidePanel;

        kahve.put("raf.showSidePanel", showSidePanel);
    }

    public Boolean getShowSidePanel() {
        return showSidePanel;
    }

    public boolean showManagerTools() {
        return showManagerTools;
    }

    public Boolean getShowRafObjectManagerTools() {
        return showRafObjectManagerTools;
    }

    public Boolean hasWriteRole(RafObject obj) throws RafException {
        return memberService.hasWriteRole(identity.getLoginName(), rafDefinition)
                || rafObjectMemberService.hasWriteRole(identity.getLoginName(), obj.getPath());
    }

    public Boolean hasDeleteRole(RafObject obj) throws RafException {
        return memberService.hasDeleteRole(identity.getLoginName(), rafDefinition)
                || rafObjectMemberService.hasDeleteRole(identity.getLoginName(), obj.getPath());
    }

    public String byteCountToDisplaySize(long bytes) {
        return FileUtils.byteCountToDisplaySize(bytes);
    }

    public Long getTotalFileCount() throws RafException {
        return rafService.getChildCount(context.getCollection().getPath());
    }

    public ContentViewPanel selectDocumentByIdAndReturnContent(String id) throws RafException {
        context.setSelectedObject(rafService.getRafObject(id));
        selectedContentPanel = getObjectContentPanel();
        return selectedContentPanel;
    }

}