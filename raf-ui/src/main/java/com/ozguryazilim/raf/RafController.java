/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.events.RafObjectDeleteEvent;
import com.ozguryazilim.raf.events.RafUploadEvent;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.AbstractContentPanel;
import com.ozguryazilim.raf.ui.base.AbstractSidePanel;
import com.ozguryazilim.raf.ui.base.ActionRegistery;
import com.ozguryazilim.raf.ui.base.ContentPanelRegistery;
import com.ozguryazilim.raf.ui.base.SidePanelRegistery;
import com.ozguryazilim.raf.ui.contentpanels.DocumentViewPanel;
import com.ozguryazilim.raf.ui.contentpanels.FolderViewPanel;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Inject @UserAware
    private Kahve kahve;
    
    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private RafService rafService;

    @Inject
    private RafContext context;

    @Inject
    private Event<RafChangedEvent> rafChangedEvent;

    @Inject
    private Event<RafFolderChangeEvent> folderChangedEvent;

    private AbstractSidePanel selectedSidePanel;

    private AbstractContentPanel selectedContentPanel;
    private AbstractContentPanel selectedCollectionContentPanel;

    @Inject
    private DocumentViewPanel  documentViewPanel;
    
    @Inject
    private FolderViewPanel  folderViewPanel;
    
    private String rafCode;

    private String objectId;

    private RafDefinition rafDefinition;
    
    private Boolean showFolders = Boolean.TRUE;

    
    @PostConstruct
    public void initDefaults(){
        showFolders = kahve.get("raf.showFolders", Boolean.TRUE).getAsBoolean();
    }
    
    /**
     * Sayfa çağrıldığında init olması için çağrılır.
     *
     * ViewAction olarak
     */
    public void init() {

        //FIXME: Bu fonksiyon parçalanıp düzenlenmeli.
        //Eğer bir şey atanmamış ise kişisel raf olsun.
        if (Strings.isNullOrEmpty(rafCode)) {
            rafCode = "PRIVATE";
        }

        try {
            rafDefinition = rafDefinitionService.getRafDefinitionByCode(rafCode);
        } catch (RafException ex) {
            //FIXME: Burada ne yapmalı?
            LOG.error("Error", ex);
        }

        //FIXME: burada aslında hala bir hata durumu var. parametre olarak alınan RAF'a erişim yetkisi olmayabilir. Ya da öyle bir raf gerçekten olmayabilir.
        context.setSelectedRaf(rafDefinition);

        if (!Strings.isNullOrEmpty(objectId)) {

            try {
                //Demek ki istenilen bir nesne var. Önce onu bir bulalım.
                RafObject obj = rafService.getRafObject(objectId);

                RafFolder fld = null;

                //şimdi objenin tipine bakarak bazı kararlar verelim
                if (obj instanceof RafDocument) {

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
                
                context.setSelectedObject(obj);

            } catch (RafException ex) {
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
            context.setFolders(rafService.getFolderList(context.getSelectedRaf().getCode()));
        } catch (RafException ex) {
            //FIXME: ne yapacağız?
            LOG.error("Raf Exception", ex);
        }

        rafChangedEvent.fire(new RafChangedEvent());
    }

    public String getRafCode() {
        return rafCode;
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

    public List<AbstractContentPanel> getContentPanels() {
        return ContentPanelRegistery.getPanels();
    }

    public List<AbstractContentPanel> getCollectionContentPanels() {
        return getContentPanels().stream()
                .filter(p -> p.getSupportCollection())
                .collect(Collectors.toList());
    }

    public AbstractContentPanel getSelectedContentPanel() {
        //Eğer seçili bir yoksa ilkini seçiyoruz.
        if (selectedContentPanel == null) {
            selectedContentPanel = getContentPanels().get(0);
        }
        return selectedContentPanel;
    }

    public void setSelectedContentPanel(AbstractContentPanel selectedContentPanel) {
        this.selectedContentPanel = selectedContentPanel;
        if (selectedContentPanel.getSupportCollection()) {
            this.selectedCollectionContentPanel = selectedContentPanel;
        }
    }

    /**
     * FIXME: Kullanıcı seçimlerini dikkate almak gerek. Şu anda ilk bulduğunu
     * dönüyor.
     *
     * @return
     */
    protected AbstractContentPanel getObjectContentPanel() {
        
        //FIXME: burada previeww panelleri gibi aslında mimeType'a bakarak doğru paneli seçmek lazım
        
        if( context.getSelectedObject() instanceof RafDocument ){
            return documentViewPanel;
        } else if ( context.getSelectedObject() instanceof RafFolder ){
            return folderViewPanel;
        }
        
        /*
        for (AbstractContentPanel p : getContentPanels()) {
            if (!p.getSupportCollection()) {
                return p;
            }
        }
        */
        
        //FIXME: Hiçibişi seçilmemesi durumu riski var.
        return null;
        
    }

    /**
     * FIXME: Kullanıcı seçimlerini dikkate almak gerek. Şu anda ilk bulduğunu
     * dönüyor.
     *
     * @return
     */
    protected AbstractContentPanel getCollectionContentPanel() {

        if (selectedCollectionContentPanel == null) {
            for (AbstractContentPanel p : getContentPanels()) {
                if (p.getSupportCollection()) {
                    selectedCollectionContentPanel = p;
                    return p;
                }
            }
        } else {
            return selectedCollectionContentPanel;
        }

        return null;
    }

    public void selectItem(RafObject item) {

        //FIXME: exception handling
        //tipe bakarak tek bir RafObject mi yoksa collection mı olacak seçmek lazım. Dolayısı ile hangi view seçeleceği de belirlenmiş olacak.
        if (item instanceof RafFolder) {
            selectFolderById(item.getId());
        } else if (item instanceof RafDocument) {
            selectDocument((RafDocument) item);
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

    public void selectFolderById(String folderId) {

        context.setSelectedObject(findFolder(folderId));
        folderChangedEvent.fire(new RafFolderChangeEvent());

        //FIXME: Doğru paneli nasıl seçeceğiz?
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

        for (RafFolder f : context.getFolders()) {
            if (id.equals(f.getId())) {
                return f;
            }
        }

        return null;
    }

    public void selectNode() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String nodeId = params.get("nodeId");
        if (!Strings.isNullOrEmpty(nodeId)) {
            selectFolderById(nodeId);
        }
    }

    /**
     * Geriye uygulanabilir durumdaki action'ları döndürür.
     * 
     * TODO: Burada action listesinin hazırlanması ile ilgili bir cache mekanizması düşünmek lazım. Her seferinde hesaplamak biraz sorunlu.
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
                        return a1.getOrder()< a2.getOrder()? -1 : a1.getOrder()> a2.getOrder()? 1 : 0;
                    }
                })
                .collect( Collectors.groupingBy(AbstractAction::getGroup,Collectors.toList()) );

        
        for( Integer g : actGroups.keySet() ){
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
        LOG.info("RafObjectDeleteEvent");
        //Ne olursa olsun. Bişi silinmiş ise selectedObject kalmamıştır
        context.setSelectedObject(null);
        //Silinen nesneyi context collection'ınından çıkaralım
        context.getCollection().getItems().remove(event.getPayload());
        //Seçili olan panel'i düzeltelim ve collection panele geçelim
        selectedContentPanel = getCollectionContentPanel();

    }

    /**
     * Birşeyler upload edildiğinde çağırılır.
     *
     * @param event
     */
    public void uploadListener(@Observes RafUploadEvent event) {
        LOG.info("RafUploadEvent");
        //Collection'ı yeniden çekmek lazım.
        selectFolderById(context.getCollection().getId());

    }

    public void folderChangeListener(@Observes RafFolderChangeEvent event) {
        //FIXME: exception handling
        //FIXME: tipe bakarak tek bir RafObject mi yoksa collection mı olacak seçmek lazım. Dolayısı ile hangi view seçeleceği de belirlenmiş olacak.
        try {
            populateFolderCollection(context.getSelectedObject().getId());

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
        //Collection'ı yeniden çekmek lazım.
        //selectFolderById(context.getCollection().getId());
        try {
            context.setFolders(rafService.getFolderList(context.getSelectedRaf().getCode()));
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
    }

    protected void populateFolderCollection( String folderId) throws RafException{
        
        RafCollection collection = rafService.getCollection(folderId);
        
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
    
    public void toggleShowFolders(){
        showFolders = !showFolders;
        
        kahve.put("raf.showFolders", showFolders);
        
        folderChangedEvent.fire(new RafFolderChangeEvent());
    }
    
    public Boolean getShowFolders(){
        return showFolders;
    }
}
