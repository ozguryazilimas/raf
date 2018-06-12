/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.google.common.base.CaseFormat;
import com.ozguryazilim.raf.ui.previewpanels.DefaultPreviewPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class PreviewPanelRegistery {
    private static final Logger LOG = LoggerFactory.getLogger(PreviewPanelRegistery.class);
 
    private static final Map<String, PreviewPanel> panels = new HashMap<>();
    private static final Map<String, String> mimeMap = new HashMap<>();
    
    
    public static void register( String name, PreviewPanel a ){
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        panels.put(name, a);
        mimeMap.put(a.mimeType(), name);
        
        LOG.info("PreviewPanel Registered : {}", name);
    }
    
    public static List<AbstractPreviewPanel> getPanels(){
        List<AbstractPreviewPanel> result = new ArrayList<>();
        for( String pn : panels.keySet()){
            result.add((AbstractPreviewPanel) BeanProvider.getContextualReference( pn, true));
        }
        
        return result;
    }
    
    /**
     * Verilen mimeType'ı destekleyen panel'i döndürür.
     * @param mimeType
     * @return 
     */
    public static AbstractPreviewPanel getMimeTypePanel( String mimeType){
        
        for( Map.Entry<String,String> ent : mimeMap.entrySet()){
            if( mimeType.startsWith( ent.getKey() )){
                return (AbstractPreviewPanel) BeanProvider.getContextualReference( ent.getValue(), true);
            }
        }
        
        //Eğer mimeType uyan bulunamadıysa default dönelim.
        return (AbstractPreviewPanel) BeanProvider.getContextualReference( DefaultPreviewPanel.class, true);
        
    }
}
