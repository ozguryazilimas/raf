/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.deltaspike.core.util.ProxyUtils;

/**
 * RafObject tipi Content paneller için default değerlerin tanımlanması yapıldı.
 * 
 * @author Hakan Uygun
 */
public interface ObjectContentViewPanel extends ContentViewPanel{
    
    @Override
    default String getName() {
        return getClass().getSimpleName();
    }

    @Override
    default boolean getSupportCollection() {
        return false;
    }

    @Override
    default String getActionIcon(){
        ContentPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.actionIcon())){
            return a.actionIcon();
        }
        
        return "panel.action.icon." + getClass().getSimpleName();
    }

    @Override
    default String getActionTitle() {
        ContentPanel a = getAnnotation();

        if (!Strings.isNullOrEmpty(a.actionTitle())) {
            return a.actionTitle();
        }

        return "panel.action.title." + getClass().getSimpleName();
    }

    @Override
    default boolean getSupportPaging() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    default boolean getSupportBreadcrumb() {
        return true;
    }

    @Override
    default String getPermission() {
        return "";
    }

    @Override
    default boolean isSupportMetadata() {
        return true;
    }

    default ContentPanel getAnnotation() {
        return (ContentPanel) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(ContentPanel.class);
    }
    
    /**
     * Veilecek nesneyi kabul ediyor mu?
     * Buna göre sunum için kullanılabilir ya da kullanılamaz.
     * @param object
     * @return 
     */
    boolean acceptObject( RafObject object );
    
    void setRafObject(RafObject object);
}
