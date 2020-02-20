package com.ozguryazilim.raf.externaldoc;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.externaldoc.config.ExternalDocPages;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@MetadataPanel(type = "externalDocWF:metadata", view = ExternalDocPages.ExternalDocWFMetadataPanel.class)
public class ExternalDocWFMetadataPanel extends AbstractMetadataPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalDocWFMetadataPanel.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

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
            m.setType("externalDocWF:metadata");
            m.getAttributes().put("externalDocWF:documentId", "");
            m.getAttributes().put("externalDocWF:documentWFId", "");
            m.getAttributes().put("externalDocWF:startedDate", new Date());
            m.getAttributes().put("externalDocWF:starter", "");
            m.getAttributes().put("externalDocWF:state", "");
            m.getAttributes().put("externalDocWF:completeDate", new Date());
            m.getAttributes().put("externalDocWF:completer", "");
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

    public String formatDate(Date dt) {
        if (dt != null) {
            return sdf.format(dt);
        } else {
            return "";
        }
    }

}
