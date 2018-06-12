/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.primefaces.context.RequestContext;

/**
 * Metadata sunum katmanı için taban sınıf.
 * 
 * @author Hakan Uygun
 */
public abstract class AbstractMetadataPanel implements Serializable{
    
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
        MetadataPanel a = getAnnotation();
        
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
        MetadataPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.title())){
            return a.title();
        }
        
        return "panel.title." + getClass().getSimpleName();
    }
    
    public String getViewId(){
        return viewConfigResolver.getViewConfigDescriptor(getView()).getViewId();
    }
    
    public String getEditorId(){
        String viewId = viewConfigResolver.getViewConfigDescriptor(getEditor()).getViewId();
        return viewId.substring(0, viewId.indexOf(".xhtml"));
    }
    
    public Class<? extends ViewConfig> getView(){
        return getAnnotation().view();
    }
    
    public Class<? extends ViewConfig> getEditor(){
        return getAnnotation().editor();
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "SınıfAdı" şeklinde permission domain tanımı döner.
     * 
     * @return 
     */
    public String getPermission(){
        
        MetadataPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.permission())){
            return a.permission();
        }
        
        return getClass().getSimpleName();
    }
    
    protected MetadataPanel getAnnotation(){
        return (MetadataPanel) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(MetadataPanel.class);
    }

    public boolean canEdit(){
        boolean b = !getAnnotation().editor().equals(ViewConfig.class);
        //FIXME: yetki kontrolü yapılacak
        return b;
    }
    
    
    /**
     * Editor dialoğu açılmadan hemen önce çağrılır ki gereken model hazırlanabilsin
     */
    protected void initEditModel(){
        
    }
    
    /**
     * UI'dan sakla talimatı geldiğinde çağrılır.
     */
    protected void save(){
        
    }
    
    public void edit(){
        initEditModel();
        Map<String, Object> options = new HashMap<>();
        RequestContext.getCurrentInstance().openDialog(getEditorId(), options, null);
    }
    
    public void closeDialog(){
        save();
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
    public void cancelDialog(){
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
}
