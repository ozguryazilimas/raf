/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.raf.models.RafFolder;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 * Context'e bağlı olarak Folder listesi sunan FolderSidePanel için controller sınıfı.
 * 
 * FIXME: burada context değişimini anlamak için bir event mekanizmasına ihtiyacımız var.
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class FolderSidePanel implements SidePanel, Serializable{

    @Inject
    private RafContext context;
    
    @Inject
    private RafService rafService;
    
    List<RafFolder> folders;
    
    @Override
    public String getTitle() {
        return context.getSelectedRaf().getName();
    }

    @Override
    public String getIcon() {
        return "fa-folder";
    }

    @Override
    public String getFragment() {
        return "/fragments/folderSidePanel.xhtml";
    }
    
    public List<RafFolder> getFolders(){
        if( folders == null ){
            try {
                folders = rafService.getFolderList(context.getSelectedRaf().getCode());
            } catch (RafException ex) {
                //FIXME: ne yapacağız?
                Logger.getLogger(FolderSidePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return folders;
    }
    
    /**
     * Context üzerinde raf değiştiğinde folder listesini null'uyoruz böylece lazım olduğunda yeniden doldurulacak.
     * @param event 
     */
    public void listedRafChange( @Observes RafChangedEvent event){
        folders = null;
    }
}
