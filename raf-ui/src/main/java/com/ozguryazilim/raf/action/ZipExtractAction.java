package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.inject.Inject;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-file-zip-o",
        capabilities = {ActionCapability.CollectionViews, ActionCapability.DetailViews},
        includedMimeType = "application/zip"
)
public class ZipExtractAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(ZipExtractAction.class);

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

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
