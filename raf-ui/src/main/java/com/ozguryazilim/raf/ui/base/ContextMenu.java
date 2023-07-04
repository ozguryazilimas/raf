package com.ozguryazilim.raf.ui.base;

import javax.enterprise.inject.Stereotype;
import javax.faces.bean.RequestScoped;
import javax.inject.Named;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Right Click Options
 */
@Stereotype
@RequestScoped
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Named
@Documented
public @interface ContextMenu {

    /**
     * Menü'de gözükecek metin. Yoksa varsayılan naming convention üzerinden bir i18 key üretecek.
     *
     * @return
     */
    String label() default "";

    /**
     * Url destekliyor ise getUrl() methodu çalıştıracak.
     *
     * @return
     */
    boolean supportUrl() default false;

    /**
     * Url destekliyor ise yeni sekmede açsın mı?
     *
     * @return
     */
    boolean openNewTab() default false;

    /**
     * Sıralama için kullanılacak
     *
     * @return
     */
    int order() default 0;

    /**
     * Ajax destekliyor mu?
     */
    boolean supportAjax() default true;

    boolean isGlobal() default false;

}
