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
public class ContentPanelRegistery {
    private static final Logger LOG = LoggerFactory.getLogger(ContentPanelRegistery.class);
 
    private static final Map<String, ContentPanel> panels = new HashMap<>();
    
    
    public static void register( String name, ContentPanel a ){
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        panels.put(name, a);
        
        LOG.info("ContentPanel Registered : {}", name);
    }
    
    public static List<ContentViewPanel> getPanels(){
        List<ContentViewPanel> result = new ArrayList<>();
        for( String pn : panels.keySet()){
            result.add((ContentViewPanel) BeanProvider.getContextualReference( pn, true));
        }
        
        return result;
    }
}
