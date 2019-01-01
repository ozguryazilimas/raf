package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
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
    private FacesContext facesContext;

    @Inject
    private RafService rafService;

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
        RafObject o = getContext().getSelectedObject();
        downloadFile((RafDocument) o);
        commandSender.sendCommand( EventLogCommandBuilder.forRaf("RAF")
                .eventType("DownloadDocument")
                .forRafObject(o)
                .message("event.DownloadDocument$%&" + identity.getUserName()+ "$%&" + o.getTitle())
                .user(identity.getLoginName())
                .build());
        return true;
    }

    public void downloadFile(RafDocument doc) {
        
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
