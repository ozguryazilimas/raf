package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.DownloadService;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.raf.utils.RafObjectUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.messages.Messages;
import javax.faces.context.FacesContextWrapper;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-download",
        capabilities = {ActionCapability.CollectionViews, ActionCapability.DetailViews},
        excludeMimeType = "raf/folder")
public class DownloadAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadAction.class);

    @Inject
    private DownloadService downloadService;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

    @Override
    protected void initActionModel() {
        LOG.info("File {} dowloaded", getContext().getSelectedObject().getPath());
    }

    @Override
    public boolean isEnabled() {
        if (RafObjectUtils.isEmptyRecord(getContext().getSelectedObject())) {
           return false;
        }

        return super.isEnabled();
    }

    @Override
    public boolean isSupportConfirmation() {
        return isSupportConfirmation(getContext().getSelectedObject());
    }

    public boolean isSupportConfirmation(RafObject obj) {
        if (obj == null || obj.getLength() == null) {
            return false;
        }

        long minDocLength = getMinimumObjectSizeForConfirmation();

        if (minDocLength < 0) {
            return false;
        }

        return obj.getLength() > minDocLength;
    }

    public long getMinimumObjectSizeForConfirmation() {
        return ConfigResolver.resolve("downloadAction.confirmation.minimumSize")
                .as(Long.class)
                .withDefault(-1L)
                .withCurrentProjectStage(false)
                .getValue();
    }

    @Override
    public String customConfirmationMessage() {
        return getDownloadConfirmationMessage(getContext().getSelectedObject());
    }

    public String getDownloadConfirmationMessage(RafObject object) {
        return Messages.getMessage("DownloadAction.confirmation.size", FileUtils.byteCountToDisplaySize(object.getLength()));
    }

    @Override
    protected boolean finalizeAction() {
        RafObject doc = getContext().getSelectedObject();
        if (doc instanceof RafRecord) {
            RafRecord record = (RafRecord) doc;
            doc = record.getDocuments().isEmpty() ? doc : record.getDocuments().get(0);
        }
        downloadFile(doc);
        commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                .eventType("DownloadDocument")
                .forRafObject(doc)
                .message("event.DownloadDocument$%&" + identity.getUserName() + "$%&" + doc.getTitle())
                .user(identity.getLoginName())
                .build());
        return true;
    }

    public void downloadFile(RafObject doc) {
        try {
            downloadService.writeFileDataToResponse(doc);
            FacesContextWrapper.getCurrentInstance().responseComplete();
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("File cannot downloded", ex);
            FacesMessages.error("error.download.failed");
        }
    }

}
