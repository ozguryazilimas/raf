package com.ozguryazilim.raf.ui.base;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

public class ContextMenuExtension implements Extension {
    /**
     * ContextMenu ile işaretli sınıfları bulup Registery'e yerleştirir.
     *
     * @param <T>
     * @param pat
     */
    <T> void processAnnotatedType(@Observes @WithAnnotations(ContextMenu.class) ProcessAnnotatedType<T> pat) {

        ContextMenu cm = pat.getAnnotatedType().getAnnotation(ContextMenu.class);
        String name = pat.getAnnotatedType().getJavaClass().getSimpleName();

        ContextMenuRegistery.register(name, cm);
    }
}
