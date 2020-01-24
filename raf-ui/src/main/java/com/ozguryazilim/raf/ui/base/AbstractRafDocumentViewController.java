package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.action.FileUploadAction;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.events.RafCheckInEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.models.RafVersion;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
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

    @Inject
    private FacesContext facesContext;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private RafContext rafContext;

    private List<RafVersion> versions = null;

    private Boolean versionManagementEnabled;

    public Boolean getHasRafWritePermission() {
        if (rafContext != null) {
            try {
                boolean permission = false;
                if (rafContext.getSelectedObject() != null && !Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(rafContext.getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), rafContext.getSelectedObject().getPath())) {
                    permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), rafContext.getSelectedObject().getPath());
                } else {
                    permission = rafContext.getSelectedRaf().getId() > 0 && memberService.hasWriteRole(identity.getLoginName(), rafContext.getSelectedRaf());
                }
                return permission;
            } catch (RafException ex) {
                LOG.error("RafException", ex);
                return false;
            }
        }
        return false;
    }

    public Boolean getExternalDocument() {
        Boolean rval = false;
        if (rafContext != null && rafContext.getSelectedObject() != null) {
//            return !externalDocRepository.findByRafFilePath(rafContext.getSelectedObject().getPath()).isEmpty();
            return false;//FIXME : Cihan : Node path içerisinde gerekli kontrolleri yap.
        }
        return rval;
    }

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
            if (getObject().getHasPreview()) {
                return PreviewPanelRegistery.getMimeTypePanel(getObject().getPreviewMimeType()).getViewId();
            } else {
                return PreviewPanelRegistery.getMimeTypePanel(getObject().getMimeType()).getViewId();
            }
        } else {
            return PreviewPanelRegistery.getMimeTypePanel("default").getViewId();
        }

    }

    public boolean getRafCheckStatus() {
        try {
            return rafService.getRafCheckStatus(getObject().getPath());
        } catch (RafException ex) {
            LOG.error("RafException", ex);
            return false;
        }
    }

    public String getRafCheckerUser() {
        try {
            return rafService.getRafCheckerUser(getObject().getPath());
        } catch (RafException ex) {
            LOG.error("RafException", ex);
            return "";
        }
    }

    public String getRafCheckOutPreviousVersion() {
        try {
            return rafService.getRafCheckOutPreviousVersion(getObject().getPath());
        } catch (RafException ex) {
            LOG.error("RafException", ex);
            return "";
        }
    }

    public Boolean getCanRafCheckIn() {
        return (identity.isPermitted("admin") || identity.getUserName().equals(getRafCheckerUser()));
    }

    public void lockFile() {
        RafObject rafObject = getObject();
        try {
            if (rafService.getRafCheckStatus(rafObject.getPath())) {
                FacesMessages.error("Dosya başka bir kullanıcı tarafından kilitlenmiş.", String.format("Kilitleyen kullanıcı : %s", rafService.getRafCheckerUser(rafObject.getPath()))); //FIXME : i118
            } else {
                rafService.setRafCheckOutValue(rafObject.getPath(), Boolean.TRUE, identity.getUserName(), new Date());
                setObject((RafDocument) rafService.getRafObject(getObject().getId()));
                FacesMessages.info("Dosya kilitlendi."); //FIXME : i118                
            }
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
    }

    public void unlockFile() {
        RafObject rafObject = getObject();
        try {
            if (!rafService.getRafCheckStatus(rafObject.getPath())) {
                FacesMessages.error("Dosyanın yazma kilidi zaten açık.");//FIXME : i118
            } else {
                rafService.setRafCheckOutValue(rafObject.getPath(), Boolean.FALSE, identity.getUserName(), new Date());
                setObject((RafDocument) rafService.getRafObject(getObject().getId()));
                FacesMessages.info("Dosya kilidi açıldı.");//FIXME : i118                
            }
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
    }

    public void checkin() {
        fileUploadAction.execute("CHECKIN", getObject().getPath());
    }

    public void externalDocInfoAction() {

    }

    public void checkInListener(@Observes RafCheckInEvent event) {
        if (getObject() == null) {
            return;
        }
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

    //FIXME: Bu methodu komple bir Action ( Örneğin DownloadAction ) haline getirmek makul bir davranış olacak sanırım.
    public void downloadHistoryContent(String version) {
        try {
            InputStream is = rafService.getDocumentVersionContent(getObject().getId(), version);

            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.setContentType(getObject().getMimeType());

            //FIXME: Dosya uzantısını doğru vermek lazım. Ama mimeType ile çalışmak lazım bir yandan da :( 
            // Dosya adına sürüm numarasını da bir şekilde eklemek faydalı olabilir. + "-" + version
            response.setHeader("Content-disposition", "attachment;filename=" + getObject().getName());

            //FIXME: RafObject içine en azından RafDocument içine boyut ve hash bilgisi yazmak lazım.
            //response.setContentLength((int) content.getProperty("jcr:data").getBinary().getSize());
            try (OutputStream out = response.getOutputStream()) {
                IOUtils.copy(is, out);
                out.flush();
            }

            //FIXME: aslında eski sürümü olduğunu belirmek lazım.
            commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                    .eventType("DownloadDocument")
                    .forRafObject(getObject())
                    .message("event.DownloadDocument$%&" + identity.getUserName() + "$%&" + getObject().getTitle())
                    .user(identity.getLoginName())
                    .build());

            facesContext.responseComplete();
        } catch (RafException | IOException ex) {
            //FIXME: i18n
            LOG.error("File cannot downloded", ex);
            FacesMessages.error("File cannot downloaded");
        }

    }

    public Boolean getVersionManagementEnabled() {
        return versionManagementEnabled;
    }

    public void downloadFile() {

        RafObject doc = getObject();
        if (doc instanceof RafRecord) {
            RafRecord record = (RafRecord) doc;
            doc = record.getDocuments().isEmpty() ? doc : record.getDocuments().get(0);
        }

        commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                .eventType("DownloadDocument")
                .forRafObject(doc)
                .message("event.DownloadDocument$%&" + identity.getUserName() + "$%&" + doc.getTitle())
                .user(identity.getLoginName())
                .build());

        //FIXME: Yetki kontrolü ve event fırlatılacak
        try {
            InputStream is = rafService.getDocumentContent(doc.getId());

            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.setContentType(doc.getMimeType());

            response.setHeader("Content-disposition", "attachment;filename=" + doc.getName());
            //FIXME: RafObject içine en azından RafDocument içine boyut ve hash bilgisi yazmak lazım.
            //response.setContentLength((int) content.getProperty("jcr:data").getBinary().getSize());

            try (OutputStream out = response.getOutputStream()) {
                IOUtils.copy(is, out);
                out.flush();
            }

            facesContext.responseComplete();
        } catch (RafException | IOException ex) {
            //FIXME: i18n
            LOG.error("File cannot downloded", ex);
            FacesMessages.error("File cannot downloaded");
        }
    }
}
