/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public @interface ContentPanel {
    String icon() default "";
    String title() default "";
    Class<? extends ViewConfig> view();
    String permission() default "";
    String actionIcon() default "";
    String actionTitle() default "";
    boolean supportPaging() default false;
    boolean supportBreadcrumb() default true;
}
