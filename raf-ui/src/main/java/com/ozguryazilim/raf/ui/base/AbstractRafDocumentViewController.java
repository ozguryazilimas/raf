package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.action.FileUploadAction;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.events.RafCheckInEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.models.RafVersion;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.share.RafShareService;
import com.ozguryazilim.raf.ui.base.metadatapanels.ShareMetadataPanel;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.raf.utils.UrlUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
    private RafMemberService rafMemberService;

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private ShareMetadataPanel shareMetadataPanel;

    @Inject
    private RafContext context;

    private List<RafVersion> versions = null;

    private Boolean versionManagementEnabled;

    private Boolean readerEnabled;

    @Override
    protected void addCustomMetadataPanel(List<AbstractMetadataPanel> list) {
        shareMetadataPanel.setObject(getObject());
        list.add(shareMetadataPanel);
    }

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
                if (RafPathUtils.isInSharedRaf(getObject().getPath())) {
                    permission = memberService.hasWriteRole(identity.getLoginName(), rafDefinitionService.getSharedRaf()) && memberService.hasCheckoutRole(identity.getLoginName(), rafDefinitionService.getSharedRaf());
                } else if (getObject() != null && !Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getObject().getPath())) {
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

    public Boolean getHasRafCheckoutPermission() {
        if (getObject() != null) {
            try {
                boolean permission = false;

                if (RafPathUtils.isInSharedRaf(getObject().getPath())) {
                    permission = memberService.hasCheckoutRole(identity.getLoginName(), rafDefinitionService.getSharedRaf());
                } else if (!Strings.isNullOrEmpty(getObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getObject().getPath())) {
                    permission = rafPathMemberService.hasCheckoutRole(identity.getLoginName(), getObject().getPath());
                } else {
                    permission = getRafFromObject() != null && memberService.hasCheckoutRole(identity.getLoginName(), getRafFromObject());
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

    private boolean hasManagerRoleOnObjectsDirectory() throws RafException {
        String path = getObject().getPath().substring(0, getObject().getPath().length() - getObject().getName().length());
        boolean hasMemberInPath = rafPathMemberService.hasMemberInPath(identity.getLoginName(), path);
        return (hasMemberInPath ? rafPathMemberService.hasManagerRole(identity.getLoginName(), path) : rafMemberService.hasManagerRole(identity.getLoginName(), context.getSelectedRaf()));
    }

    public Boolean getCanRafCheckIn() throws RafException {
        return (identity.isPermitted("admin") || identity.getUserName().equals(getRafCheckerUser()) || hasManagerRoleOnObjectsDirectory());
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
                            int versionNum1 = NumberUtils.toInt(t.getName().replaceAll("\\.", ""), 0);
                            int versionNum2 = NumberUtils.toInt(t1.getName().replaceAll("\\.", ""), 0);
                            return versionNum2 - versionNum1;
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
            FacesMessages.error("error.download.failed");
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
            FacesMessages.error("error.download.failed");
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