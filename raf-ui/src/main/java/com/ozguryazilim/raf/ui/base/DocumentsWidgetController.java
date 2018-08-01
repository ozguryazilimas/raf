/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.models.RafObject;
import java.util.List;
import org.primefaces.event.SelectEvent;

/**
 * UI üzerinde DocumentsWidget için Controller sınıfları için API
 * @author Hakan Uygun
 */
public interface DocumentsWidgetController {
    
    /**
     * Geriye Widget'ın sunacağı RafObject listesini döndürür.
     * @return 
     */
    List<? extends RafObject> getRafObjects();
    
    /**
     * Yeni bir belge buradan yüklenebilir mi?
     * @return 
     */
    default Boolean getCanUpload(){
        return Boolean.FALSE;
    }
    
    /**
     * Var olan belgelerden biri bu listeye eklenebilir mi?
     * @return 
     */
    default Boolean getCanAdd(){
        return Boolean.FALSE;
    }
    
    /**
     * Listeden bir belge çıkarılabilir mi?
     * @return 
     */
    default Boolean getCanRemove(){
        return Boolean.FALSE;
    }
    
    /**
     * Listeden bir belge indirilebilir mi?
     * @return 
     */
    default Boolean getCanDownload(){
        return Boolean.FALSE;
    }
    
    /**
     * Eğer Upload destekleniyor ise implemente edilmeli
     */
    default void upload(){
        
    }
    
    /**
     * Upload destekleniyorsa upload işlemi bittikten sonra çağrılır.
     * Tazaleme işlemleri için kullanılır.
     */
    default void onUploadComplete(){
        
    }
    
    /**
     * RafLookup sonrası geri dönüş kontrolü. Add için
     * @param event 
     */
    default void onAddDocumentSelect(SelectEvent event) {
        
    }
    
    /**
     * Eğer Ekleme destekleniyor ise implemente edilmeli
     */
    default void addDocument(){
        
    }
    
    /**
     * Eğer çıkarma destekleniyor ise implemente edilmeli
     */
    default void removeDocument( RafObject object ){
        
    }
    
}
