package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.action.CreateFolderAction;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafMetadata;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metadata sunum katmanı için taban sınıf.
 * 
 * @author Hakan Uygun
 */
public abstract class AbstractMetadataPanel implements Serializable{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetadataPanel.class);

    @Inject
    private ViewConfigResolver viewConfigResolver;

    @Inject
    private RafContext context;

    private RafMetadata metadata;

    public RafMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(RafMetadata metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Varsayılan hali ile sınıf adını döner.
     * @return 
     */
    public String getName(){
        return getClass().getSimpleName();
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.icon." + SınıfAdı şeklinde i18n'den almaya çalışır.
     * 
     * @return 
     */
    public String getIcon(){
        MetadataPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.icon())){
            return a.icon();
        }
        
        return "panel.icon." + getClass().getSimpleName();
    }

    public RafContext getContext() {
        return this.context;
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "panel.title." + SınıfAdı şeklinde i18n'den almaya çalışır.
     * 
     * @return 
     */
    public String getTitle(){
        MetadataPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.title())){
            return a.title();
        }
        
        return "panel.title." + getClass().getSimpleName();
    }
    
    public String getViewId(){
        return viewConfigResolver.getViewConfigDescriptor(getView()).getViewId();
    }
    
    public String getEditorId(){
        String viewId = viewConfigResolver.getViewConfigDescriptor(getEditor()).getViewId();
        return viewId.substring(0, viewId.indexOf(".xhtml"));
    }
    
    public Class<? extends ViewConfig> getView(){
        return getAnnotation().view();
    }
    
    public Class<? extends ViewConfig> getEditor(){
        return getAnnotation().editor();
    }
    
    /**
     * Annotation'a bakar eğer orada tanılı olan bir şey yoksa "SınıfAdı" şeklinde permission domain tanımı döner.
     * 
     * @return 
     */
    public String getPermission(){
        
        MetadataPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.permission())){
            return a.permission();
        }
        
        return getClass().getSimpleName();
    }
    
    public int getOrder(){
        return getAnnotation().order();
    }
    
    protected MetadataPanel getAnnotation(){
        return (MetadataPanel) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(MetadataPanel.class);
    }

    public boolean canEdit(){
        boolean b = !getAnnotation().editor().equals(ViewConfig.class);
        //FIXME: yetki kontrolü yapılacak
        return b;
    }
    
    
    /**
     * Editor dialoğu açılmadan hemen önce çağrılır ki gereken model hazırlanabilsin
     */
    protected void initEditModel(){
        
    }
    
    /**
     * UI'dan sakla talimatı geldiğinde çağrılır.
     */
    protected void save(){
        
    }
    
    public void edit(){
        initEditModel();
        Map<String, Object> options = new HashMap<>();
        RequestContext.getCurrentInstance().openDialog(getEditorId(), options, null);
    }
    
    public void closeDialog(){
        save();
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
    public void cancelDialog(){
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
}
