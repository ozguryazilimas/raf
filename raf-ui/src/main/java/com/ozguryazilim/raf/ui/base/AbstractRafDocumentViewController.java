package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.action.FileUploadAction;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
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
import org.apache.commons.io.IOUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * RafDocument view controlü için taban sınıf.
 *
 * Üzerinde View Id, PreviewPanel ile ilgili kontroller bulunuyor.
 *
 * @author Hakan Uygun
 */
public class AbstractRafDocumentViewController extends AbstractRafObjectViewController<RafDocument> {
    private static final String eventLogTokenSeperator = "$%&";

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
    private RafDefinitionService rafDefinitionService;

    private List<RafVersion> versions = null;

    private Boolean versionManagementEnabled;

    private Boolean readerEnabled;

    private RafDefinition getRafFromObject() {
        if (getObject() != null && !Strings.isNullOrEmpty(getObject().getPath()) && getObject().getPath().contains("/")) {
            try {
                return rafDefinitionService.getRafDefinitionByCode(getObject().getPath().split("/")[2]);
            } catch (RafException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    public Boolean getHasRafWritePermission() {
        if (getObject() != null) {
            try {
                boolean permission = false;
                if (getObject() != null && !Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getObject().getPath())) {
                    permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getObject().getPath());
                } else {
                    permission = getRafFromObject() != null && memberService.hasWriteRole(identity.getLoginName(), getRafFromObject());
                }
                return permission;
            } catch (RafException ex) {
                LOG.error("RafException", ex);
                return false;
            }
        }
        return false;
    }

    @PostConstruct
    public void init() {
        versionManagementEnabled = "true".equals(ConfigResolver.getPropertyValue("raf.version.enabled", "false"));
        readerEnabled = "true".equals(ConfigResolver.getPropertyValue("raf.reader.enabled", "true"));
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

    /**
     * UI'da "incele" butonu gözüksün mü? belgenin mimetype'ına kayıtlı bir reader varsa gözükecektir.
     *
     * @return
     */
    public boolean isHaveReaderPage() {
        if (!readerEnabled || getObject() == null || getObject().getMimeType() == null) return false;
        return RafReaderRegistery.isAnyReaderPageForGivenMimeType(getObject().getMimeType());
    }

    /**
     * Mimetype'a uygun reader sayfasını dönüyoruz.
     */
    public String getReaderPage() {
        return RafReaderRegistery.getMimeTypePanel(getObject().getMimeType()).getViewId();
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
            response.setHeader("Content-disposition", String.format("attachment;filename=\"%s\"", getObject().getName()));

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

    public void revertVersion(String version, String versionComment) {
        try {
            InputStream is = rafService.getDocumentVersionContent(getObject().getId(), version);
            rafService.checkin(getObject().getPath(), is, versionComment);

            StringJoiner sj = new StringJoiner(eventLogTokenSeperator);
            String eventMessage = sj.add("event.RevertVersion")
            .add(identity.getUserName())
            .add(getObject().getTitle())
            .add(version)
            .toString();

            commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                    .eventType("RevertVersion")
                    .forRafObject(getObject())
                    .message(eventMessage)
                    .user(identity.getLoginName())
                    .build());

            facesContext.responseComplete();
            setObject((RafDocument) rafService.getRafObject(getObject().getId()));
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("File cannot revert to version", ex);
            FacesMessages.error("File cannot revert to version");
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

            response.setHeader("Content-disposition", String.format("attachment;filename=\"%s\"", doc.getName()));
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

    public void reGeneratePreview() {
        try {
            rafService.regeneratePreview(getObject().getId());
        } catch (RafException e) {
            LOG.error("Cannot regenerate preview", e);
        }
    }
}
