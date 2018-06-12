/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.google.common.base.CaseFormat;
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
public class MetadataPanelRegistery {
    
    private static final Logger LOG = LoggerFactory.getLogger(MetadataPanelRegistery.class);
 
    private static final Map<String, MetadataPanel> panels = new HashMap<>();
    
    /**
     * nodeType ( metadata ) panel mappingi
     * Bir tip için birden fazla panel register edilebilir.
     */
    private static final Map<String, List<String>> typeMap = new HashMap<>();
    
    
    public static void register( String name, MetadataPanel a ){
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        panels.put(name, a);
        
        List<String> l = typeMap.get(a.type());
        if( l == null ){
            l = new ArrayList<>();
            typeMap.put(a.type(),l);
        }
        
        l.add(name);
        
        LOG.info("MetadataPanel Registered : {}", name);
    }
    
    public static List<AbstractMetadataPanel> getPanels(){
        List<AbstractMetadataPanel> result = new ArrayList<>();
        for( String pn : panels.keySet()){
            result.add((AbstractMetadataPanel) BeanProvider.getContextualReference( pn, true));
        }
        
        return result;
    }
    
    public static List<AbstractMetadataPanel> getPanels( String type ){
        List<AbstractMetadataPanel> result = new ArrayList<>();
        
        List<String> l = typeMap.get(type);
        
        if( l != null ){
            for( String pn : l){
                result.add((AbstractMetadataPanel) BeanProvider.getContextualReference( pn, true));
            }
        }
        
        return result;
    }
}
