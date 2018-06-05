/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafFolder;
import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 * Context'e bağlı olarak Folder listesi sunan FolderSidePanel için controller sınıfı.
 * 
 * FIXME: Folder seçildiğinde ne yapılacak? Seçili Folder UI'a nasıl yansıtılacak.
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class FolderSidePanel implements SidePanel, Serializable{

    @Inject
    private RafContext context;
    
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
        return context.getFolders();
    }
}
