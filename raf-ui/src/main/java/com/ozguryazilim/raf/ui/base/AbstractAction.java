package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.models.RafObject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.primefaces.context.RequestContext;

/**
 *
 * @author oyas
 */
public class AbstractAction implements Serializable {

    @Inject
    private ViewConfigResolver viewConfigResolver;

    @Inject
    private RafContext context;

    /**
     * Varsayılan hali ile sınıf adını döner.
     *
     * @return
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.icon." +
     * SınıfAdı şeklinde i18n'den almaya çalışır.
     *
     * @return
     */
    public String getIcon() {
        Action a = getAnnotation();

        if (!Strings.isNullOrEmpty(a.icon())) {
            return a.icon();
        }

        return "action.icon." + getClass().getSimpleName();
    }

    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.title." +
     * SınıfAdı şeklinde i18n'den almaya çalışır.
     *
     * @return
     */
    public String getTitle() {
        Action a = getAnnotation();

        if (!Strings.isNullOrEmpty(a.title())) {
            return a.title();
        }

        return "action.title." + getClass().getSimpleName();
    }

    public String getDailogId() {
        String dlgId = viewConfigResolver.getViewConfigDescriptor(getDialog()).getViewId();
        return dlgId.substring(0, dlgId.indexOf(".xhtml"));
    }

    public Class<? extends ViewConfig> getDialog() {
        return getAnnotation().dialog();
    }

    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "SınıfAdı"
     * şeklinde permission domain tanımı döner.
     *
     * @return
     */
    public String getPermission() {

        Action a = getAnnotation();

        if (!Strings.isNullOrEmpty(a.permission())) {
            return a.permission();
        }

        return getClass().getSimpleName();
    }

    protected Action getAnnotation() {
        return (Action) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(Action.class);
    }

    public boolean hasDialog() {
        boolean b = !getAnnotation().dialog().equals(ViewConfig.class);
        //FIXME: yetki kontrolü yapılacak
        return b;
    }

    /**
     * Editor dialoğu açılmadan hemen önce çağrılır ki gereken model
     * hazırlanabilsin
     */
    protected void initActionModel() {

    }

    /**
     * Aslında action ile ilgili asıl işlem burada yapılacak.
     *
     * @return
     */
    protected boolean finalizeAction() {
        return true;
    }

    public void execute() {

        //eğer enabled değilse hemen çıkalım
        if (!isEnabled()) {
            return;
        }

        initActionModel();

        //Eğer gösterilecek bir UI yoksa ise doğrudan işleme gidelim.
        if (hasDialog()) {
            openDialog();
        } else {
            finalizeAction();
        }
    }

    protected void initDialogOptions(Map<String, Object> options) {

    }

    protected void openDialog() {
        Map<String, Object> options = new HashMap<>();

        options.put("modal", true);
        //options.put("draggable", false);
        options.put("resizable", false);

        //options.put("responsive",   true);
        //options.put("fitViewport", true );
        initDialogOptions(options);

        RequestContext.getCurrentInstance().openDialog(getDailogId(), options, null);
    }

    public void closeDialog() {

        //Eğer geriye false geliyor ise dialoğu kapatma. Mesaj felan vardır.
        if (!finalizeAction()) {
            return;
        }

        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void cancelDialog() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public RafContext getContext() {
        return context;
    }

    public void setContext(RafContext context) {
        this.context = context;
    }

    /**
     * Bu action context'e uygulanabilir mi?
     *
     * FIXME: Multiple nesne için bu methodun elden geçmesi gerek.
     *
     * TODO: Daha okunur ve DRY için parçalanması gerek. özellikle mimeType
     * checkleri için bir utility iyi olur.
     *
     * @param forCollection uygulanmak istenilen yer Bir Collection UI'i mı?
     * @return
     */
    public boolean applicable(boolean forCollection) {
        String im = getAnnotation().includedMimeType();
        String em = getAnnotation().excludeMimeType();

        //Eğer Collection için isteniyor ise
        if (forCollection && hasCapability(ActionCapability.CollectionViews)) {

            String mm = getContext().getCollection().getMimeType();

            //Exclude var mı?
            if (!Strings.isNullOrEmpty(em)) {
                //Exclude'a uyuyor o zaman hemen false ile çıkalım.
                if (mm.startsWith(em)) {
                    return false;
                }
            }

            if (!Strings.isNullOrEmpty(im)) {
                //Herşeye OK miş
                if ("*".equals(im)) {
                    return true;
                } else if (im.contains(",")) {
                    //mimeType virgül içeriyor, demekki birden fazla tip isteniyor.
                    String[] mimeTypes = im.split(",");
                    boolean result = false;
                    int i = 0;
                    while (!result && i < mimeTypes.length) {
                        result = mm.startsWith(mimeTypes[i]);
                        i++;
                    }
                    return result;
                } else if (mm.startsWith(im)) {
                    //Evet kabul edilebilir mimeType
                    return true;
                }
            }
        } else if (!forCollection && hasCapability(ActionCapability.DetailViews)) {
            RafObject o = getContext().getSelectedObject();

            //Kontrol edilebilecek bir seçim yok!
            if (o == null) {
                return false;
            }

            String mm = getContext().getSelectedObject().getMimeType();

            //Exclude var mı?
            if (!Strings.isNullOrEmpty(em)) {
                //Exclude'a uyuyor o zaman hemen false ile çıkalım.
                if (mm.startsWith(em)) {
                    return false;
                }
            }

            if (!Strings.isNullOrEmpty(im)) {
                //Herşeye OK miş
                if ("*".equals(im)) {
                    return true;
                } else if (im.contains(",")) {
                    //mimeType virgül içeriyor, demekki birden fazla tip isteniyor.
                    String[] mimeTypes = im.split(",");
                    boolean result = false;
                    int i = 0;
                    while (!result && i < mimeTypes.length) {
                        result = mm.startsWith(mimeTypes[i]);
                        i++;
                    }
                    return result;
                } else if (mm.startsWith(im)) {
                    //Evet kabul edilebilir mimeType
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isSupportAjax() {
        return hasCapability(ActionCapability.Ajax);
    }

    public boolean isSupportConfirmation() {
        return hasCapability(ActionCapability.Confirmation);
    }

    public boolean hasCapability(ActionCapability cap) {
        return Arrays.asList(getAnnotation().capabilities()).contains(cap);
    }

    public int getGroup() {
        return getAnnotation().group();
    }

    public int getOrder() {
        return getAnnotation().order();
    }

    /**
     * Contexted göre enabled olup olmadığı bilgisi.
     *
     * Capabilityler üzerinden varsayılan hal hesaplanmaya çalışılır.
     *
     * @return
     */
    public boolean isEnabled() {

        if (hasCapability(ActionCapability.NeedClipboard) && getContext().getClipboard().isEmpty()) {
            return false;
        }

        if (hasCapability(ActionCapability.NeedSelection) && getContext().getSeletedItems().isEmpty()) {
            return false;
        }

        return true;
    }
}
