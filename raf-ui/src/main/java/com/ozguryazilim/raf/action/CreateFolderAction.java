/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.telve.messages.FacesMessages;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Seçili klasör içine yeni bir kalsör ekler.
 * 
 * @author Hakan Uygun
 */
@Action(dialog = ActionPages.CreateFolderDialog.class,icon = "fa-plus",supportCollection = true,includedMimeType = "raf/folder")
public class CreateFolderAction extends AbstractAction{
    
    @Inject
    private RafService rafService;
    
    @Inject
    private Event<RafFolderDataChangeEvent> folderCreateEvent;
    
    private RafFolder folder;

    @Override
    protected void initActionModel() {
        folder = new RafFolder();
        
        folder.setParentId(getContext().getCollection().getId());
    }

    @Override
    protected boolean finalizeAction() {
        folder.setPath( getContext().getCollection().getPath() + "/" + folder.getName());
        
        try {
            rafService.createFolder(folder);
        } catch (RafException ex) {
            //TODO: i18n
            FacesMessages.error("Raf Tanımlaması Yapılamadı", ex.getMessage());
            return false;
        }
        
        folderCreateEvent.fire(new RafFolderDataChangeEvent());
        
        return true;
    }
    
    public RafFolder getFolder() {
        return folder;
    }

    public void setFolder(RafFolder folder) {
        this.folder = folder;
    }
    
}
