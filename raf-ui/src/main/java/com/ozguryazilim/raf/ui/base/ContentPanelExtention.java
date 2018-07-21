/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class ContentPanelExtention implements Extension{
    
    private static final Logger LOG = LoggerFactory.getLogger(ContentPanelExtention.class);
    
    /**
     * ContentPanel ile işaretli sınıfları bulup Registery'e yerleştirir.
     * @param <T>
     * @param pat 
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(ContentPanel.class) ProcessAnnotatedType<T> pat) {

        ContentPanel a = pat.getAnnotatedType().getAnnotation(ContentPanel.class);
        String name = pat.getAnnotatedType().getJavaClass().getSimpleName();
        
        //ContentViewPanel'den üretilmemiş olanlar içeri alınmayacak
        if( !ContentViewPanel.class.isAssignableFrom(pat.getAnnotatedType().getJavaClass())){
            LOG.warn("ContentPanel definition must implements ContentViewPanel : {} not registered", name);
        } else {
            ContentPanelRegistery.register( name, a);
        }
        
    }
}
