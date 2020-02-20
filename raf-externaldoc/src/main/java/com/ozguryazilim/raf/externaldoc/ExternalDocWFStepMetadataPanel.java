package com.ozguryazilim.raf.externaldoc;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.externaldoc.config.ExternalDocPages;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import com.ozguryazilim.telve.messages.FacesMessages;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@MetadataPanel(type = "externalDocWFStep:metadata", view = ExternalDocPages.ExternalDocWFStepMetadataPanel.class)
public class ExternalDocWFStepMetadataPanel extends AbstractMetadataPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalDocWFStepMetadataPanel.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafContext context;

    @Override
    protected void initEditModel() {
        super.initEditModel();
        RafMetadata m = getMetadata();
        if (m == null) {
            m = new RafMetadata();
            m.setType("externalDocWFStep:metadata");
            m.getAttributes().put("externalDocWFStep:documentWFId", "");
            m.getAttributes().put("externalDocWFStep:startedDateTime", "");
            m.getAttributes().put("externalDocWFStep:starter", "");
            m.getAttributes().put("externalDocWFStep:state", "");
            m.getAttributes().put("externalDocWFStep:completeDateTime", "");
            m.getAttributes().put("externalDocWFStep:completer", "");
            m.getAttributes().put("externalDocWFStep:stepName", "");
            m.getAttributes().put("externalDocWFStep:comment", "");
            setMetadata(m);
        }
    }

    @Override
    protected void save() {
        LOG.info("Metadata : {}", getMetadata());
        try {
            rafService.saveMetadata(context.getSelectedObject().getId(), getMetadata());
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Metadata Kaydı yapılamadı.", ex);
            FacesMessages.error("Metadata Kaydı yapılamadı.");
        }
        super.save();
    }

}
