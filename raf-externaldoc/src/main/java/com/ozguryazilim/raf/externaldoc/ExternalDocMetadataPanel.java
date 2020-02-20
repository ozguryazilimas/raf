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
@MetadataPanel(type = "externalDoc:metadata", view = ExternalDocPages.ExternalDocMetadataPanel.class)
public class ExternalDocMetadataPanel extends AbstractMetadataPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalDocMetadataPanel.class);
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
            m.setType("externalDoc:metadata");
            m.getAttributes().put("externalDoc:documentCreateDate", new Date());
            m.getAttributes().put("externalDoc:documentFolder", "");
            m.getAttributes().put("externalDoc:documentFormat", "");
            m.getAttributes().put("externalDoc:documentName", "");
            m.getAttributes().put("externalDoc:documentParentFolder", "");
            m.getAttributes().put("externalDoc:documentType", "");
            m.getAttributes().put("externalDoc:documentStatus", "");

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
