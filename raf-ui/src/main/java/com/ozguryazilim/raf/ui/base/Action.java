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
public @interface Action {
    
    String icon() default "";
    
    String title() default "";
    
    /**
     * Bir action eğer dialog açıyor ise burada sayfa belirtilmesi lazım
     * @return 
     */
    Class<? extends ViewConfig> dialog() default ViewConfig.class;
    
    String permission() default "";
    /**
     * Action'ın desteklediği mimeType'lar için test patterni
     * 
     * Örnek: image/ girilir ise image/ ile başlayanlar destekleniyor demek.
     * 
     * TODO: Regex mi olsa?
     * @return 
     */
    String includedMimeType() default "*";
    
    /**
     * Action'ının desteklemediği mimeType'lar
     * 
     * Örnek : raf/folder
     * 
     * @return 
     */
    String excludeMimeType() default "";
    
    /**
     * Bu action birden fazla nesne için çalışabilir mi?
     * @return 
     */
    boolean supportMultipleObject() default false;
    
    /**
     * Bu action RafCollection tipi için çalışabilir mi?
     * @return 
     */
    boolean supportCollection() default false;
    
    boolean supportAjax() default true;
}
