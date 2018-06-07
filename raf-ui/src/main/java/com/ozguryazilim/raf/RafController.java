/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.ui.sidepanels.FolderSidePanel;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractSidePanel;
import com.ozguryazilim.raf.ui.base.SidePanelRegistery;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 * Temel Raf arayüzü controller sınıfı.
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class RafController implements Serializable{
   
    @Inject
    private Identity identity;
    
    @Inject
    private RafDefinitionService rafDefinitionService;
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafContext context;
    
    @Inject
    private SimpleRowViewPanel simpleRowView;
    
    @Inject
    private DetailRowViewPanel detailRowView;
    
    @Inject
    private DocumentViewPanel documentView;
    
    @Inject
    private FolderViewPanel folderView;
    
    @Inject
    private Event<RafChangedEvent> rafChangedEvent;
    
    @Inject
    private Event<RafFolderChangeEvent> folderChangedEvent;
    
    private AbstractSidePanel selectedSidePanel;
    
    private ContentPanel selectedContentPanel;
    
    private String rafCode;
    
    private String objectId;
    
    private RafDefinition rafDefinition;
    
    /**
     * Sayfa çağrıldığında init olması için çağrılır.
     * 
     * ViewAction olarak
     */
    public void init(){
        
        //FIXME: Bu fonksiyon parçalanıp düzenlenmeli.
        
        //Eğer bir şey atanmamış ise kişisel raf olsun.
        if( Strings.isNullOrEmpty(rafCode)){
            rafCode = "PRIVATE";
        }
        
        try {
            rafDefinition = rafDefinitionService.getRafDefinitionByCode(rafCode);
        } catch (RafException ex) {
            //FIXME: Burada ne yapmalı?
            Logger.getLogger(RafController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //FIXME: burada aslında hala bir hata durumu var. parametre olarak alınan RAF'a erişim yetkisi olmayabilir. Ya da öyle bir raf gerçekten olmayabilir.
        
        context.setSelectedRaf(rafDefinition);

        if( !Strings.isNullOrEmpty(objectId)){
            
            try {
                //Demek ki istenilen bir nesne var. Önce onu bir bulalım.
                RafObject obj = rafService.getRafObject(objectId);
                
                RafFolder fld = null;
                
                //şimdi objenin tipine bakarak bazı kararlar verelim
                if( obj instanceof RafDocument ){
                    
                    //Folder'ı bir bulalım
                    //TODO: tip kontrolü yapmaya gerek var mı?
                    fld = (RafFolder) rafService.getRafObject(obj.getParentId());
                    selectedContentPanel = documentView;
                    
                } else if( obj instanceof RafFolder ){
                    fld = (RafFolder) obj;
                    //FIXME: burada kullanıcı tercihlerinden alınması gerekir.
                    selectedContentPanel = simpleRowView;
                }
                
                
                context.setCollection(rafService.getCollection(fld.getId()));
                context.setSelectedObject(obj);
                
            } catch (RafException ex) {
                Logger.getLogger(RafController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            
        } else {
            //Demek permalink olarak istenen bişi yok. O zaman rootu alalım.
            //FIXME: burda exception handling gerekli
            RafCollection collection = null;
            try {
                collection = rafService.getCollection(rafDefinition.getNodeId());
            } catch (RafException ex) {
                Logger.getLogger(RafController.class.getName()).log(Level.SEVERE, null, ex);
            }
            context.setCollection(collection);
            //FIXME: bundan çok emin değilim. RafFolder değil RafNode bağladık çünkü.
            //context.setSelectedObject(rafDefinition.getNode());
        }
        
        
        try {
                context.setFolders( rafService.getFolderList(context.getSelectedRaf().getCode()));
        } catch (RafException ex) {
                //FIXME: ne yapacağız?
                Logger.getLogger(FolderSidePanel.class.getName()).log(Level.SEVERE, null, ex);
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
    public List<AbstractSidePanel> getSidePanels(){
        return SidePanelRegistery.getSidePanels();
    }

    public AbstractSidePanel getSelectedSidePanel() {
        //Eğer seçili bir yoksa ilkini seçiyoruz.
        if( selectedSidePanel == null ){
            selectedSidePanel = getSidePanels().get(0);
        }
        return selectedSidePanel;
    }

    public void setSelectedSidePanel(AbstractSidePanel selectedSidePanel) {
        this.selectedSidePanel = selectedSidePanel;
    }
    

    public List<ContentPanel> getContentPanels(){
        List<ContentPanel> result = new ArrayList<>();
        
        result.add(simpleRowView);
        result.add(detailRowView);
        result.add(folderView);
        
        return result;
    }

    public ContentPanel getSelectedContentPanel() {
        //Eğer seçili bir yoksa ilkini seçiyoruz.
        if( selectedContentPanel == null ){
            selectedContentPanel = getContentPanels().get(0);
        }
        return selectedContentPanel;
    }

    public void setSelectedContentPanel(ContentPanel selectedContentPanel) {
        this.selectedContentPanel = selectedContentPanel;
    }


    public void selectItem( RafObject item){
        
        //FIXME: exception handling
        
        //tipe bakarak tek bir RafObject mi yoksa collection mı olacak seçmek lazım. Dolayısı ile hangi view seçeleceği de belirlenmiş olacak.
        if( item instanceof RafFolder ){
            selectFolderById(item.getId());
        } else if( item instanceof RafDocument ){
            selectDocument((RafDocument)item);
        }
        
    }
    
    public void selectDocument( RafDocument item){
        context.setSelectedObject(item);
        selectedContentPanel = documentView;
    }
    
    public void selectFolderById( String folderId ){
        
        //FIXME: exception handling
        
        //FIXME: tipe bakarak tek bir RafObject mi yoksa collection mı olacak seçmek lazım. Dolayısı ile hangi view seçeleceği de belirlenmiş olacak.
        RafCollection collection = null;
        try {
            collection = rafService.getCollection(folderId);
        } catch (RafException ex) {
            Logger.getLogger(RafController.class.getName()).log(Level.SEVERE, null, ex);
        }
        context.setCollection(collection);
        
        try {
                context.setFolders( rafService.getFolderList(context.getSelectedRaf().getCode()));
        } catch (RafException ex) {
                //FIXME: ne yapacağız?
                Logger.getLogger(FolderSidePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        folderChangedEvent.fire(new RafFolderChangeEvent());
        context.setSelectedObject(findFolder(folderId));
        
        selectedContentPanel = simpleRowView;
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
        if( !Strings.isNullOrEmpty(nodeId)){
            selectFolderById(nodeId);
        }
    }
}
