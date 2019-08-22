package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.action.FileUploadAction;
import com.ozguryazilim.raf.events.RafCheckInEvent;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafVersion;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RafDocument view controlü için taban sınıf.
 *
 * Üzerinde View Id, PreviewPanel ile ilgili kontroller bulunuyor.
 *
 * @author Hakan Uygun
 */
public class AbstractRafDocumentViewController extends AbstractRafObjectViewController<RafDocument> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRafDocumentViewController.class);

    @Inject
    private FileUploadAction fileUploadAction;

    @Inject
    private RafService rafService;

    private List<RafVersion> versions = null;

    private Boolean versionManagementEnabled;

    @PostConstruct
    public void init() {
        versionManagementEnabled = "true".equals(ConfigResolver.getPropertyValue("raf.version.enabled", "false"));
    }

    @Override
    public void setObject(RafDocument object) {
        super.setObject(object);
        versions = null;
    }

    @Override
    public String getViewId() {
        return "/fragments/documentView.xhtml";
    }

    /**
     * Geriye mimeType'a göre hangi widget kullanılacak ise onun fragman
     * bilgisini döner.
     *
     * @return
     */
    public String getPreviewWidget() {
        //Eğer mimetype yoksa default isteyelim
        if (getObject() != null) {
            if( getObject().getHasPreview()){
                return PreviewPanelRegistery.getMimeTypePanel(getObject().getPreviewMimeType()).getViewId();
            } else {
                return PreviewPanelRegistery.getMimeTypePanel(getObject().getMimeType()).getViewId();
            }
        } else {
            return PreviewPanelRegistery.getMimeTypePanel("default").getViewId();
        }

    }

    public void checkin() {
        fileUploadAction.execute("CHECKIN", getObject().getPath());
    }
    
    
    public void checkInListener( @Observes RafCheckInEvent event ){
        if (getObject() == null) return;
        
        try { 
            setObject((RafDocument) rafService.getRafObject(getObject().getId()));
            versions = null;
        } catch (RafException ex) {
            LOG.error("Version Upload Error", ex);
        }
    }

    public List<RafVersion> getVersionHistory() {

        if (versions == null) {
            if (getVersionManagementEnabled()) {
                try {
                    versions = rafService.getVersionHistory(getObject());
                    
                    //FIXME: Aslında burada sort order string üzerinden doğru çalışmaz ama sürüm sayısı az iken idare eder.
                    versions.sort(new Comparator<RafVersion>() {
                        @Override
                        public int compare(RafVersion t, RafVersion t1) {
                            return t.getName().compareTo(t1.getName()) * -1;
                        }
                    });
                } catch (RafException ex) {
                    LOG.error("Raf Exception", ex);
                    FacesMessages.error(ex.getMessage());
                    versions = new ArrayList<>();
                }
            } else {
                versions = new ArrayList<>();
            }
        }

        return versions;
    }

    public Boolean getVersionManagementEnabled() {
        return versionManagementEnabled;
    }

    
}
