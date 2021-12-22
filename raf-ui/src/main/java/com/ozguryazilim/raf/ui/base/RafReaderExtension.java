package com.ozguryazilim.raf.ui.base;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

public class RafReaderExtension implements Extension {

    /**
     * CommandEditor ile işaretli sınıfları bulup Registery'e yerleştirir.
     *
     * @param <T>
     * @param pat
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(RafReader.class) ProcessAnnotatedType<T> pat) {

        RafReader a = pat.getAnnotatedType().getAnnotation(RafReader.class);
        String name = pat.getAnnotatedType().getJavaClass().getSimpleName();
        RafReaderRegistery.register(name, a);
    }
}
