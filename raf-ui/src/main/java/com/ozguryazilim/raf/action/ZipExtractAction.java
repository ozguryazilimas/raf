package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author oyas
 */
@Action(icon = "fa-file-zip-o",
        capabilities = {ActionCapability.CollectionViews, ActionCapability.DetailViews},
        includedMimeType = "application/zip"
)
public class ZipExtractAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(ZipExtractAction.class);

    @Inject
    private RafService rafService;

    @Override
    protected boolean finalizeAction() {
        if ("application/zip".equals(getContext().getSelectedObject().getMimeType())) {
            try {
                RafObject zipFile = getContext().getSelectedObject();
                rafService.extractZipFile(zipFile);
            } catch (Exception ex) {
                LOG.error("RafException", ex);
                FacesMessages.error("Hata", ex.getMessage());
            }
        }
        return super.finalizeAction();
    }

}
