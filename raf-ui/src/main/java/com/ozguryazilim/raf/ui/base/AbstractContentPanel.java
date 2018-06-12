/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import java.io.Serializable;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;

/**
 * Content Paneller için taban sınıf.
 * 
 * @author Hakan Uygun
 */
public abstract class AbstractContentPanel implements Serializable{
    
    @Inject
    private ViewConfigResolver viewConfigResolver;

    /**
     * Varsayılan hali ile sınıf adını döner.
     * @return 
     */
    public String getName(){
        return getClass().getSimpleName();
    }
    
    /**
     * Bu içerik sunumu colleciton destekliyor mu?
     * @return 
     */
    public abstract boolean getSupportCollection();
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.icon." + SınıfAdı şeklinde i18n'den almaya çalışır.
     * 
     * @return 
     */
    public String getIcon(){
        ContentPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.icon())){
            return a.icon();
        }
        
        return "panel.icon." + getClass().getSimpleName();
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.title." + SınıfAdı şeklinde i18n'den almaya çalışır.
     * 
     * @return 
     */
    public String getTitle(){
        ContentPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.title())){
            return a.title();
        }
        
        return "panel.title." + getClass().getSimpleName();
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.icon." + SınıfAdı şeklinde i18n'den almaya çalışır.
     * 
     * @return 
     */
    public String getActionIcon(){
        ContentPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.actionIcon())){
            return a.actionIcon();
        }
        
        return "panel.action.icon." + getClass().getSimpleName();
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.title." + SınıfAdı şeklinde i18n'den almaya çalışır.
     * 
     * @return 
     */
    public String getActionTitle(){
        ContentPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.actionTitle())){
            return a.actionTitle();
        }
        
        return "panel.action.title." + getClass().getSimpleName();
    }
    
    public boolean getSupportPaging(){
        ContentPanel a = getAnnotation();
        return a.supportPaging();
    } 
    
    public boolean getSupportBreadcrumb(){
        ContentPanel a = getAnnotation();
        return a.supportBreadcrumb();
    } 
    
    public String getViewId(){
        return viewConfigResolver.getViewConfigDescriptor(getView()).getViewId();
    }
    
    public Class<? extends ViewConfig> getView(){
        return getAnnotation().view();
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "SınıfAdı" şeklinde permission domain tanımı döner.
     * 
     * @return 
     */
    public String getPermission(){
        
        ContentPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.permission())){
            return a.permission();
        }
        
        return getClass().getSimpleName();
    }
    
    protected ContentPanel getAnnotation(){
        return (ContentPanel) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(ContentPanel.class);
    }
}
