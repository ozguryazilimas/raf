package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ozguryazilim.raf.ApplicationContstants;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * @author oyas
 */
public class AbstractAction implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAction.class);

    @Inject
    private ViewConfigResolver viewConfigResolver;

    @Inject
    private RafContext context;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private RafMemberService rafMemberService;

    @Inject
    private Identity identity;

    protected Cache<String, Boolean> cache;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Varsayılan hali ile sınıf adını döner.
     *
     * @return
     */
    public String getName() {
        return ProxyUtils.getUnproxiedClass(getClass()).getSimpleName();
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

        return "action.icon." + ProxyUtils.getUnproxiedClass(getClass()).getSimpleName();
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

        return "action.title." + ProxyUtils.getUnproxiedClass(getClass()).getSimpleName();
    }

    public String getDailogId() {
        String dlgId = viewConfigResolver.getViewConfigDescriptor(getDialog()).getViewId();
        return dlgId.substring(0, dlgId.indexOf(".xhtml"));
    }

    public Class<? extends ViewConfig> getDialog() {
        return getAnnotation().dialog();
    }

    /**
     * Annotation'a bakar ve tanılı olan rolleri döner.
     * Eğer tanılı olan rol yoksa Action ın herhangi bir yetkiden erişilebilir olduğu çıkarılır.
     *
     * @return
     */
    public Set<String> getPermissions() {
        return new HashSet<>(Arrays.asList(getAnnotation().permissions()));
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

        StringBuilder cacheKeyBuilder = new StringBuilder();
        cacheKeyBuilder.append(forCollection);
        cacheKeyBuilder.append(getContext().getCollection() != null ? getContext().getCollection().getId() : null);
        cacheKeyBuilder.append(getContext().getSelectedObject() != null ? getContext().getSelectedObject().getId() : null);
        cacheKeyBuilder.append(getContext().getSelectedRaf() != null ? getContext().getSelectedRaf().getId() : null);
        try {
            return cache.get(cacheKeyBuilder.toString(), () -> {
                String im = getAnnotation().includedMimeType();
                String em = getAnnotation().excludeMimeType();

                boolean sharedRafActionPermissionsEnabled = ConfigResolver.resolve("raf.shared.enable.action.permission")
                        .as(Boolean.class)
                        .withDefault(Boolean.TRUE)
                        .getValue();
                LOG.debug("Shared Raf action permissions enabled: {}", sharedRafActionPermissionsEnabled);
                if (sharedRafActionPermissionsEnabled) {
                    String actionPermission = getAnnotation().actionPermission();
                    boolean hasPermissionOnSharedRaf = !actionPermission.isEmpty() ? identity.hasPermission("sharedRaf", actionPermission) : true;

                    if (ApplicationContstants.SHARED_RAF.equals(getContext().getSelectedRaf().getCode()) && !hasPermissionOnSharedRaf) {
                        return false;
                    }
                }

                //Eğer Collection için isteniyor ise
                if (forCollection && hasCapability(ActionCapability.CollectionViews) && getContext().getCollection() != null) {
                    String mm = getContext().getCollection().getMimeType();
                    return isApplicableMimeType(mm);

                } else if (!forCollection && hasCapability(ActionCapability.DetailViews) && getContext().getSelectedObject() != null) {
                    RafObject object = getContext().getSelectedObject();
                    if (object == null) {
                        return false;
                    }

                    return isApplicableMimeType(object.getMimeType());
                }

                return false;
            });
        } catch (ExecutionException ex) {
            LOG.error("Error", ex);
            return false;
        }
    }

    public boolean isApplicableMimeType(String mimeType) {
        String im = getAnnotation().includedMimeType();
        String em = getAnnotation().excludeMimeType();

        //Exclude var mı?
        if (!Strings.isNullOrEmpty(em)) {
            //Exclude'a uyuyor o zaman hemen false ile çıkalım.
            if (mimeType.startsWith(em)) {
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
                    result = mimeType.startsWith(mimeTypes[i]);
                    i++;
                }
                return result;
            } else if (mimeType.startsWith(im)) {
                //Evet kabul edilebilir mimeType
                return true;
            }
        }

        return false;
    }

    public boolean permitted(String loginName) {
        StringBuilder cacheKeyBuilder = new StringBuilder();
        cacheKeyBuilder.append(loginName);
        cacheKeyBuilder.append(getContext().getSelectedRaf() != null ? getContext().getSelectedRaf().getId() : null);
        cacheKeyBuilder.append(getContext().getSelectedObject() != null ? getContext().getSelectedObject().getId() : null);
        cacheKeyBuilder.append(getAnnotation().actionPermission());
        try {
            return cache.get(cacheKeyBuilder.toString(), () -> {
                if (StringUtils.isBlank(loginName)) {
                    return false;
                }

                boolean sharedRafActionPermissionsEnabled = ConfigResolver.resolve("raf.shared.enable.action.permission")
                        .as(Boolean.class)
                        .withDefault(Boolean.TRUE)
                        .getValue();

                if (sharedRafActionPermissionsEnabled) {
                    String actionPermission = getAnnotation().actionPermission();
                    boolean hasPermissionOnSharedRaf = !actionPermission.isEmpty() ? identity.hasPermission("sharedRaf", actionPermission) : true;

                    if (ApplicationContstants.SHARED_RAF.equals(getContext().getSelectedRaf().getCode()) && !hasPermissionOnSharedRaf) {
                        return false;
                    }
                }

                if (getContext().getSelectedRaf() != null && ApplicationContstants.SHARED_RAF.equals(getContext().getSelectedRaf().getCode())) {
                    return true;
                } else if (getContext().getSelectedObject() != null && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(loginName, getContext().getSelectedObject().getPath())) {
                    return rafPathMemberService.hasMemberAnyRole(loginName, getPermissions(), getContext().getSelectedObject().getPath());
                } else {
                    return getContext().getSelectedRaf() != null && rafMemberService.hasMemberAnyRole(loginName, getPermissions(), getContext().getSelectedRaf());
                }
            });
        } catch (ExecutionException ex) {
            LOG.error("Raf Exception", ex);
            return false;
        }
    }

    public boolean isSupportAjax() {
        return hasCapability(ActionCapability.Ajax);
    }

    public boolean isSupportConfirmation() {
        return hasCapability(ActionCapability.Confirmation);
    }

    public String customConfirmationMessage() {
        return null;
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

        if (hasCapability(ActionCapability.NeedSelection)) {

            if (getContext().getSeletedItems().isEmpty()) {
                return false;
            }

            String includedSelectionMimeType = getAnnotation().includedSelectionMimeType();
            String excludedSelectionMimeType = getAnnotation().excludedSelectionMimeType();
            List<String> selectedItemsMimeTypes = getContext().getSeletedItems().stream().map(RafObject::getMimeType).collect(Collectors.toList());

            if (!selectedItemsMimeTypes.isEmpty()) {
                if (!Strings.isNullOrEmpty(excludedSelectionMimeType)) {
                    if (selectedItemsMimeTypes.stream().anyMatch(mime -> mime.startsWith(excludedSelectionMimeType))) {
                        return false;
                    }
                }
                if (!Strings.isNullOrEmpty(includedSelectionMimeType)) {
                    if (!selectedItemsMimeTypes.stream().anyMatch(mime -> mime.startsWith(includedSelectionMimeType))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean hasChangeableStateIcon(){
        return hasCapability(ActionCapability.ChangeableStateIcon);
    }

    public String getChangeableStateIcon() {
        // Alt sınıflar tarafından implemente edilebilir.
        return null;
    }
}
