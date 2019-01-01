package com.ozguryazilim.raf.ui.base;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

/**
 *
 * @author oyas
 */
public class SidePanelExtention implements Extension{
    /**
     * CommandEditor ile işaretli sınıfları bulup Registery'e yerleştirir.
     * @param <T>
     * @param pat 
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(SidePanel.class) ProcessAnnotatedType<T> pat) {

        SidePanel a = pat.getAnnotatedType().getAnnotation(SidePanel.class);
        String name = pat.getAnnotatedType().getJavaClass().getSimpleName();
        
        //FIXME:Burada implementasyona bakılacak ve AbstractSidePanel'den üretilmemiş ise hata verilecek.
        
        //name.to
        SidePanelRegistery.register( name, a);
    }
}
