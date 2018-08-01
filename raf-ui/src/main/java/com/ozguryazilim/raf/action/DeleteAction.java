/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.events.RafObjectDeleteEvent;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-trash", capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection, ActionCapability.Confirmation},
        group = 11,
        order = 0)
public class DeleteAction extends AbstractAction{

    private static final Logger LOG = LoggerFactory.getLogger(DeleteAction.class);
    
    @Inject 
    private RafService rafService;
    
    @Inject
    private Event<RafObjectDeleteEvent> deleteEvent;
    
    @Inject
    private Event<RafFolderDataChangeEvent> folderChangeEvent;
    
    @Override
    protected void initActionModel() {
        LOG.info("Delete Execute");
        super.initActionModel(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean finalizeAction() {

        List<RafObject> items = new ArrayList<>(getContext().getSeletedItems());
        for( RafObject o : items){
            deleteObject(o);
        }
        //FIXME: Burada RafEventLog çalıştırılmalı
        getContext().getSeletedItems().clear();
        
        return true;
    }
    
    public void deleteObject( RafObject o ){
        try {
            rafService.deleteObject(o);
            deleteEvent.fire(new RafObjectDeleteEvent(o));
            
            //Eğer silinen şey folder ise FolderChangeEvent'ide fırlatalım ki folder ağaçları da düzenlensin
            if( o instanceof RafFolder){
                folderChangeEvent.fire(new RafFolderDataChangeEvent());
            }
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Delete Error", ex);
            FacesMessages.error("Kayıt silinemedi!");
        }
    }
    
}
