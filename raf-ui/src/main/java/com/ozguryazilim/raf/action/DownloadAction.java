package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.DownloadService;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
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
    private RafService rafService;

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
            downloadService.downloadFile(doc);
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("File cannot downloded", ex);
            FacesMessages.error("File cannot downloaded");
        }
    }

}
