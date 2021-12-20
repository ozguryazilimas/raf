package com.ozguryazilim.raf.ui.base;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.scope.WindowScoped;

import javax.enterprise.inject.Stereotype;
import javax.inject.Named;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Stereotype
@WindowScoped
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Named
@Documented
public @interface RafReader {
    Class<? extends ViewConfig> view();

    String permission() default "";

    /**
     * Regex pattern for mimetype(s)
     * @return
     */
    String mimeType();
}
