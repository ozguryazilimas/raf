package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.deltaspike.core.util.ProxyUtils;

import javax.inject.Inject;
import java.io.Serializable;

public abstract class AbstractContextMenuItem implements Serializable {

    @Inject
    protected RafContext context;

    public String getUrl(RafObject object) {
        // Varsayılan davranışı obje referansına giden url'i oluşturmak. Alt sınıflar tarafından değiştirilebilir.
        return "raf.jsf?id=&o=" + object.getId();
    }

    public void execute(RafObject object) throws RafException {
        // Alt sınıflar tarafından implemente edilecek.
        throw new RafException("Unknown Abstraction Implementation");
    }

    public boolean applicable() {
        // Alt sınıflar tarafından yetki kontrollerine göre restrictionlar yapılabilir.
        return true;
    }

    public boolean disabled(RafObject object) {
        // Alt sınıflar tarafından aktif/deaktif şeklinde ayarlanabilir.
        return false;
    }

    public boolean openNewTab() {
        return getAnnotation().openNewTab();
    }

    public boolean supportAjax() {
        return getAnnotation().supportAjax();
    }

    public boolean isGlobal() {
        return getAnnotation().isGlobal();
    }

    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "contextMenu.label." +
     * SınıfAdı şeklinde i18n'den almaya çalışır.
     *
     * @return
     */
    public String getLabel() {
        ContextMenu contextMenu = getAnnotation();

        if (!Strings.isNullOrEmpty(contextMenu.label())) {
            return contextMenu.label();
        }

        return "contextMenu.label." + getName();
    }

    public boolean supportUrl() {
        return getAnnotation().supportUrl();
    }

    public int getOrder() {
        return getAnnotation().order();
    }

    /**
     * Varsayılan hali ile sınıf adını döner.
     *
     * @return
     */
    public String getName() {
        return Character.toLowerCase(getClass().getSimpleName().charAt(0)) + getClass().getSimpleName().substring(1);
    }

    protected ContextMenu getAnnotation() {
        return (ContextMenu) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(ContextMenu.class);
    }
}