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
 *
 * @author oyas
 */
public abstract class AbstractSidePanel implements Serializable{
    
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
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.icon." + SınıfAdı şeklinde i18n'den almaya çalışır.
     * 
     * @return 
     */
    public String getIcon(){
        SidePanel a = getAnnotation();
        
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
        SidePanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.title())){
            return a.title();
        }
        
        return "panel.title." + getClass().getSimpleName();
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
        
        SidePanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.permission())){
            return a.permission();
        }
        
        return getClass().getSimpleName();
    }
    
    protected SidePanel getAnnotation(){
        return (SidePanel) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(SidePanel.class);
    }
    
}
