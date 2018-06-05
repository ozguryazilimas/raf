/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.models.RafFolder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 * Breadcrumb fragmanı için controller sınıfı.
 *
 * FIXME: folderlar için bir event ve cache gerek. Context üzerindeki değişimden
 * haberdar olunmalı.
 *
 * SelectedFolder değiştiğinde değişmeli.
 *
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class FolderBreadcrumbController implements Serializable {

    @Inject
    private RafContext context;

    List<RafFolder> items;
    
    RafFolder parentFolder;
    RafFolder currentFolder;

    /**
     * Geriye breadcrumb için kullanılacak folder listesini döndürür.
     *
     * FIXME: Burada bir cache sistemine ihtiyaç var. Her seferinde yeniden
     * oluşturulmamalı bunu da sanırım event ile halledebiliriz.
     *
     * @return
     */
    public List<RafFolder> getBreadcrumbItems() {

        if (items == null) {
            items = new ArrayList<>();
            if (!Strings.isNullOrEmpty(context.getCollection().getPath())) {
                String[] ss = context.getCollection().getPath().split("/");

                for (String s : ss) {
                    RafFolder f = findFolder(s);
                    if (f != null) {
                        items.add(f);
                    }
                }
                
                //En son olan current diye atanacak.
                if( items.size() > 0 ){
                    currentFolder = items.remove(items.size() -1);
                }
                
                //Şimdi sonuncusu ise bir üst folder
                if( items.size() > 0 ){
                    parentFolder = items.get(items.size() -1);
                }
            }
        }

        return items;
    }

    public RafFolder getParentFolder() {
        return parentFolder;
    }

    public RafFolder getCurrentFolder() {
        return currentFolder;
    }

    
    
    /**
     * Context'e bulunan RafFolder içinden ismi verilen folder'ı bulur. Bulamaz
     * ise null döner.
     *
     * @param name
     * @return
     */
    private RafFolder findFolder(String name) {

        for (RafFolder f : context.getFolders()) {
            if (name.equals(f.getName())) {
                return f;
            }
        }

        return null;
    }
    
    public void listener( @Observes RafFolderChangeEvent event){
        items = null;
        currentFolder = null;
        parentFolder = null;
    }
    
    public void listener( @Observes RafChangedEvent event){
        items = null;
        currentFolder = null;
        parentFolder = null;
    }
}
