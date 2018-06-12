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

/**
 *
 * @author oyas
 */
public class MetadataPanelExtention implements Extension{
 
    /**
     * MetadataPanel ile işaretli sınıfları bulup Registery'e yerleştirir.
     * @param <T>
     * @param pat 
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(MetadataPanel.class) ProcessAnnotatedType<T> pat) {

        MetadataPanel a = pat.getAnnotatedType().getAnnotation(MetadataPanel.class);
        String name = pat.getAnnotatedType().getJavaClass().getSimpleName();
        
        //FIXME:Burada implementasyona bakılacak ve AbstractMetadataPanel'den üretilmemiş ise hata verilecek.
        
        //name.to
        MetadataPanelRegistery.register( name, a);
    }
}
