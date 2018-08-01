/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.messages.FacesMessages;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-paste", 
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.NeedClipboard}, 
        includedMimeType = "raf/folder",
        group = 10,
        order = 3)
public class PasteAction extends AbstractAction{

    private static final Logger LOG = LoggerFactory.getLogger(PasteAction.class);
    
    @Inject
    private RafService rafService;
    
    @Inject
    private Event<RafFolderDataChangeEvent> folderDataChangeEvent;
    
    @Inject
    private Event<RafFolderChangeEvent> folderChangeEvent;
    
    @Override
    protected boolean finalizeAction() {
        
        if( getContext().getClipboardAction() == null ) return false;
        if( getContext().getClipboard().isEmpty() ) return false;
        
        LOG.info("Paste {} : {}", getContext().getClipboardAction().getName(), getContext().getClipboard());
        
        //TODO: aslında asıl komuta geri dönüp onu çalıştırmak daha mantıklı olacak. Böylece özelleşmiş paste komutları yazılabilir. ( link v.s. için mesela )
        if( getContext().getClipboardAction() instanceof CopyAction ){
            try {
                rafService.copyObject(getContext().getClipboard(), (RafFolder) getContext().getSelectedObject());
                //FIXME: Burada RafEventLog çalıştırılmalı
            } catch (RafException ex) {
                //FIXME: i18n
                LOG.error("Cannot copy", ex);
                FacesMessages.error("Cannot copy", ex.getLocalizedMessage());
            }
        } else if( getContext().getClipboardAction() instanceof CutAction ){
            try {
                rafService.moveObject(getContext().getClipboard(), (RafFolder) getContext().getSelectedObject());
                //FIXME: Burada RafEventLog çalıştırılmalı
            } catch (RafException ex) {
                //FIXME: i18n
                LOG.error("Cannot move", ex);
                FacesMessages.error("Cannot move", ex.getLocalizedMessage());
            }
        }
        
        folderChangeEvent.fire(new RafFolderChangeEvent());
        
        if( getContext().getClipboard().stream().anyMatch( o -> o instanceof RafFolder )){
            folderDataChangeEvent.fire(new RafFolderDataChangeEvent());
        }
        
        
        getContext().setClipboardAction(null);
        getContext().getClipboard().clear();
        
        return super.finalizeAction(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
