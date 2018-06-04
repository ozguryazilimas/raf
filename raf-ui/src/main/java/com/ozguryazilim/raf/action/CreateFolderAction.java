/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 *
 * @author oyas
 */
@SessionScoped
@Named
public class CreateFolderAction implements Serializable{
    
    @Inject
    private RafContext context;
    
    @Inject
    private RafService rafService;
    
    @Inject
    private Event<RafChangedEvent> rafChangedEvent;
    
    private RafFolder folder;
    
    public void execute(){
        
        folder = new RafFolder();
        
        folder.setParentId(context.getCollection().getId());
        
        Map<String, Object> options = new HashMap<>();

        RequestContext.getCurrentInstance().openDialog("/actions/createFolderDialog", options, null);
    }
    
    public void closeDialog() {
        
        folder.setPath( context.getCollection().getPath() + "/" + folder.getName());
        
        
        try {
            rafService.createFolder(folder);
        } catch (RafException ex) {
            //TODO: i18n
            FacesMessages.error("Raf Tanımlaması Yapılamadı", ex.getMessage());
        }
        
        rafChangedEvent.fire(new RafChangedEvent());
        
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void cancelDialog() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public RafFolder getFolder() {
        return folder;
    }

    public void setFolder(RafFolder folder) {
        this.folder = folder;
    }
    
}
