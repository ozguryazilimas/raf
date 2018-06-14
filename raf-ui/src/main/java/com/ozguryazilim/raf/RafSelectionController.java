/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.models.RafObject;
import java.io.Serializable;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class RafSelectionController implements Serializable{
    
    
    @Inject
    private RafContext context;
    
    /**
     * Bütün seçimleri siler
     */
    public void clear(){
        context.getSeletedItems().clear();
    }
    
    /**
     * Mevcut collection'daki tüm nesneleri seçer.
     */
    public void selectAll(){
        clear();
        context.getSeletedItems().addAll( context.getCollection().getItems());
    }
    
    
    public void toggleSelectAll(){
        if( isAllSelected() ){
            clear();
        } else {
            selectAll();
        }
    }
    
    /**
     * Bir nesne seçilmişse kaldırır seçilmemiş ise ekler
     * @param o 
     */
    public void toggle( RafObject o ){
        if( context.getSeletedItems().contains(o) ){
            context.getSeletedItems().remove(o);
        } else {
            context.getSeletedItems().add(o);
        }
    }
    
    public boolean isSelected( RafObject o ){
        return context.getSeletedItems().contains(o);
    }
    
    public boolean isAllSelected( ){
        return context.getSeletedItems().size() == context.getCollection().getItems().size();
    }
    
    public void folderChangeListener( @Observes RafFolderChangeEvent event){
        clear();
    }
}
