package com.ozguryazilim.raf.ui.base;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

/**
 *
 * @author oyas
 */
public class PreviewPanelExtention implements Extension{
    
    /**
     * CommandEditor ile işaretli sınıfları bulup Registery'e yerleştirir.
     * @param <T>
     * @param pat 
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(PreviewPanel.class) ProcessAnnotatedType<T> pat) {

        PreviewPanel a = pat.getAnnotatedType().getAnnotation(PreviewPanel.class);
        String name = pat.getAnnotatedType().getJavaClass().getSimpleName();
        
        //FIXME:Burada implementasyona bakılacak ve AbstractSidePanel'den üretilmemiş ise hata verilecek.
        
        //name.to
        PreviewPanelRegistery.register( name, a);
    }
}
