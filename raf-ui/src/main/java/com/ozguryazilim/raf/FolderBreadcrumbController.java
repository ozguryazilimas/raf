package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.models.RafFolder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
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

    @Inject @UserAware
    private Kahve kahve;
    
    @Inject
    private RafContext context;

    private List<RafFolder> items;
    
    private RafFolder parentFolder;
    private RafFolder currentFolder;
    
    private Boolean showBreadcrumb = Boolean.TRUE;
    
    
    @PostConstruct
    public void init(){
        showBreadcrumb = kahve.get("raf.showBreadcrumb", Boolean.TRUE).getAsBoolean();
    }

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

                String p = "";
                
                for (String s : ss) {
                    //Eğer içi boşsa yani ilk "/" ise pass geçelim. Yoksa //RAF/AAA gibi şeyler oluyor.
                    if( Strings.isNullOrEmpty(s)) continue;
                    p = p + "/" + s;
                    RafFolder f = findFolder(p);
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
     * Context'e bulunan RafFolder içinden pathi verilen folder'ı bulur. Bulamaz
     * ise null döner.
     *
     * @param name
     * @return
     */
    private RafFolder findFolder(String path) {

        for (RafFolder f : context.getFolders()) {
            if (path.equals(f.getPath())) {
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
    
    public void toggleShow(){
        showBreadcrumb = !showBreadcrumb;
        kahve.put("raf.showBreadcrumb", showBreadcrumb);
    }
    
    public Boolean getShowBreadcrumb(){
        return showBreadcrumb;
    }
}
