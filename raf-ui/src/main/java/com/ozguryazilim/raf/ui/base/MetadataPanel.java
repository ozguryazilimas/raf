package com.ozguryazilim.raf.ui.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Stereotype;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.view.ViewConfig;

/**
 *
 * @author oyas
 */
@Stereotype
@SessionScoped
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Named
@Documented
public @interface MetadataPanel {
    String icon() default "";
    String title() default "";
    Class<? extends ViewConfig> view();
    /**
     * Atanmaz ise editor yok demektir. Sadece read only bir sunum verilir.
     * @return 
     */
    Class<? extends ViewConfig> editor() default ViewConfig.class;
    String permission() default "";
    int order() default 10;
    /**
     * Ne tür metadata gösterebileceğinin setlenmesi gerekir.
     * JCR nodeType
     * Örneğin image:metadata 
     * Eğer * ya da boşluk verilir ise her zaman gösterilir.
     * @return 
     */
    String type();
}
