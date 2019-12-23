package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafCheckInEvent;
import com.ozguryazilim.raf.events.RafUploadEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.uploader.ui.FileUploadDialog;
import com.ozguryazilim.telve.uploader.ui.FileUploadHandler;
import java.io.IOException;
import java.util.Map;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-upload",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews},
        includedMimeType = "raf/folder",
        order = 0)
public class FileUploadAction extends AbstractAction implements FileUploadHandler {

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
    private FileUploadDialog fileUploadDialog;

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
    private boolean actionExec = Boolean.TRUE;
    private boolean versionManagementEnabled = Boolean.FALSE;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean permission = false;

            if (!Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
            } else {
                permission = getContext().getSelectedRaf().getId() > 0 && memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
            }

            return permission && super.applicable(forCollection);
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
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
        options.put("contentHeight", 480);
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
        //openDialog();
        fileUploadDialog.openDialog(this, "");
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
        fileUploadDialog.openDialog(this, "");
    }

    public String getRafCode() {
        return rafCode;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public String getUploadPath() {
        return uploadPath;
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

    @Override
    public void handleFileUpload(String uri) {
        LOG.debug("File Upload complete : {}", uri);

        try {
            UploadInfo uploadInfo = fileUploadService.getUploadInfo(uri);
            LOG.debug("Uploaded File : {}", uploadInfo.getFileName());

            if ("CHECKIN".equals(rafCode)) {
                rafService.checkin(getUploadPath(), fileUploadService.getUploadedBytes(uri));
            } else {
                String absPath = getUploadPath() + "/" + uploadInfo.getFileName();
                //versiyon özelliği aktif ise aynı dosya üzerine yazılmamalı, yeni versiyon eklemeli.
                if (versionManagementEnabled && checkFileExists(absPath)) {
                    throw new RafException("File is exists");
                }
                rafService.uploadDocument(absPath, fileUploadService.getUploadedBytes(uri));
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
}
