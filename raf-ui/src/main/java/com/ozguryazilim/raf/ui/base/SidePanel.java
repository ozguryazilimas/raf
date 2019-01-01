package com.ozguryazilim.raf.ui.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.inject.Stereotype;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 *
 * @author oyas
 */
@Stereotype
@WindowScoped
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Named
@Documented
public @interface SidePanel {
    String icon() default "";
    String title() default "";
    Class<? extends ViewConfig> view();
    String permission() default "";
}
