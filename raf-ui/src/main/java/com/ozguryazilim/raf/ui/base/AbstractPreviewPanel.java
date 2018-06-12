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
 * RafObjeleri için Preview panel taban sınıfı.
 * 
 * @author Hakan Uygun
 */
public class AbstractPreviewPanel implements Serializable{

    @Inject
    private ViewConfigResolver viewConfigResolver;

    /**
     * Varsayılan hali ile sınıf adını döner.
     * @return 
     */
    public String getName(){
        return getClass().getSimpleName();
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
        
        PreviewPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.permission())){
            return a.permission();
        }
        
        return getClass().getSimpleName();
    }
    
    protected PreviewPanel getAnnotation(){
        return (PreviewPanel) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(PreviewPanel.class);
    }
}
