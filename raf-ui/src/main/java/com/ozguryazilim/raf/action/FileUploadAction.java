package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafCheckInEvent;
import com.ozguryazilim.raf.events.RafUploadEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.raf.uploader.RafFileUploadDialog;
import com.ozguryazilim.raf.uploader.RafFileUploadHandler;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.messages.Messages;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-upload",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews},
        includedMimeType = "raf/folder",
        order = 0)
public class FileUploadAction extends AbstractAction implements RafFileUploadHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FileUploadAction.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafContext rafContext;

    @Inject
    private Event<RafUploadEvent> rafUploadEvent;

    @Inject
    private Event<RafCheckInEvent> rafCheckInEvent;

    @Inject
    private RafFileUploadDialog fileUploadDialog;

    @Inject
    private TusFileUploadService fileUploadService;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    private String rafCode;
    private String uploadPath;
    private RafRecord targetRecord;
    private boolean actionExec = Boolean.TRUE;
    private boolean versionManagementEnabled = Boolean.FALSE;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean permission = false;

            if (getContext().getSelectedObject() != null && !Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
            } else {
                permission = memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
            }
            
            return permission && super.applicable(forCollection);
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
    }

    public RafRecord getTargetRecord() {
        return targetRecord;
    }

    @Override
    protected void initActionModel() {
        super.initActionModel();

        rafCode = rafContext.getSelectedRaf().getCode();
        uploadPath = rafContext.getCollection().getPath();
        actionExec = Boolean.TRUE;
        versionManagementEnabled = "true".equals(ConfigResolver.getPropertyValue("raf.version.enabled", "false"));
    }

    @Override
    protected void initDialogOptions(Map<String, Object> options) {
        options.put("contentHeight", 495);
        options.put("contentWidth", 640);
    }

    @Override
    protected boolean finalizeAction() {
        if (actionExec) {
            //Eğer action düğmesinden çağrılmış ise normal UploadEventi. Böylece RafController yakalar.
            //FIXME: doğru eventi fırlatalım.
            rafUploadEvent.fire(new RafUploadEvent());
        } else {
            rafCheckInEvent.fire(new RafCheckInEvent());
        }

        return super.finalizeAction();
    }

    /**
     * Normal akış dışında bir yerden Upload çalıştırmak için kullanılır.
     *
     * Bakınız DocumentsWidgetController implementasyonları. TaskController
     *
     * @param rafCode
     * @param uploadPath
     */
    public void execute(String rafCode, String uploadPath) {
        this.rafCode = rafCode;
        this.uploadPath = uploadPath;
        actionExec = Boolean.FALSE;
        // Yeni bir versiyon eklenecekse eklenecek dosya sayısı limitini 1 yapıyoruz.
        if (rafCode.equals("CHECKIN")) {
            fileUploadDialog.openDialog(this, 1);
        } else {
            fileUploadDialog.openDialog(this);
        }
    }

    public void execute(String rafCode, String uploadPath, RafRecord targetRecord) {
        this.targetRecord = targetRecord;
        this.execute(rafCode, uploadPath);
    }

    /**
     * Burada kütüphanenin dialoğu kullanıldığı için her zaman için geriye true
     * dönecek.
     *
     * @return
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * Telve uploader'ın dialoğunu açalım.
     */
    @Override
    protected void openDialog() {
        fileUploadDialog.openDialog(this);
    }

    @Override
    public String getRafCode() {
        return rafCode;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setTargetRecord(RafRecord targetRecord) {
        this.targetRecord = targetRecord;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    private Boolean checkFileExists(String absPath) {
        try {
            RafObject rafObject = rafService.getRafObjectByPath(absPath);
            return rafObject != null;
        } catch (RafException ex) {
            return false;
        }
    }

    public void isAlreadyUploaded() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String filename = params.get("filename");
        String absPath = getUploadPath() + "/" + filename;
        if (checkFileExists(absPath)) {
            LOG.debug("{} already uploaded.", absPath);
            RequestContext.getCurrentInstance().addCallbackParam("isAlreadyUploaded", true);
            RequestContext.getCurrentInstance().addCallbackParam("value", filename);
        } else {
            RequestContext.getCurrentInstance().addCallbackParam("isAlreadyUploaded", false);
        }
    }

    @Override
    public void handleFileUpload(String uri) {
    }

    @Override
    public void handleFileUpload(String uri, boolean decompress) {
        LOG.debug("File Upload complete : {}", uri);

        try {
            UploadInfo uploadInfo = fileUploadService.getUploadInfo(uri);
            LOG.debug("Uploaded File : {}", uploadInfo.getFileName());

            String absPath = getUploadPath() + "/" + uploadInfo.getFileName();
            //versiyon özelliği aktif ise aynı dosya üzerine yazılmamalı, yeni versiyon eklemeli.
            if (versionManagementEnabled && checkFileExists(absPath)) {
                throw new RafException("File is exists");
            }
            RafDocument uploadedDocument = rafService.uploadDocument(absPath, fileUploadService.getUploadedBytes(uri));

            if (decompress && "application/zip".equals(uploadedDocument.getMimeType())) {
                rafService.extractZipFile(uploadedDocument);
                if ("true".equals(ConfigResolver.getProjectStageAwarePropertyValue("auto.extract.zip.files.remove.after.extract", "true"))) {
                    rafService.deleteObject(uploadedDocument);
                }
            }
            fileUploadService.deleteUpload(uri);
            //FIXME: burası her dosya yüklenmesinde çağrılıyor. Aslında Telve-Uploader dialogun kapandığına dair bilgi vermeli. #31635 işine bakın            
        } catch (IOException | TusException | RafException ex) {
            //FIXME: i18n
            FacesMessages.error("Files Cannot Uploaded");
            LOG.error("File Upload Error", ex);
        } finally {
            finalizeAction();
        }
    }

    @Override
    public void handleFileUpload(String uri, String versionComment) {
        try {
            rafService.checkin(getUploadPath(), fileUploadService.getUploadedBytes(uri), versionComment);
            fileUploadService.deleteUpload(uri);
        } catch (IOException | TusException | RafException ex) {
            FacesMessages.error("An error occurred while installing the new version.");
            LOG.error("An error occurred while installing the new version.", ex);
        } finally {
            finalizeAction();
        }
    }

    public String getUserFriendlyRafPath() {
        String sharedPrefix = "/SHARED";
        String privatePrefix = "/PRIVATE/" + identity.getLoginName();
        String rafPrefix = "/RAF/";

        String rafPath;

        if (uploadPath.startsWith(sharedPrefix)) {
            String localizedRafText = Messages.getMessage("raf.label.Shared");
            rafPath = uploadPath.replaceFirst(sharedPrefix, localizedRafText);
        } else if (uploadPath.startsWith(privatePrefix)) {
            String localizedRafText = Messages.getMessage("raf.label.Private");
            rafPath = uploadPath.replaceFirst(privatePrefix, localizedRafText);
        } else {
            rafPath = uploadPath.replaceFirst(rafPrefix, "");
        }
        return !rafPath.contains("/") ? rafPath + ":/" : rafPath.replaceFirst("/", ":/");
    }

}
