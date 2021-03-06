package com.ozguryazilim.raf.emaildoc;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.emaildoc.config.EmailDocPages;
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
@MetadataPanel(type = "emailDoc:metadata", view = EmailDocPages.EmailDocMetadataPanel.class)
public class EmailDocMetadataPanel extends AbstractMetadataPanel {

    private static final Logger LOG = LoggerFactory.getLogger(EmailDocMetadataPanel.class);
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
            m.setType("emailDoc:metadata");
            m.getAttributes().put("emailDoc:date", new Date());
            m.getAttributes().put("emailDoc:messageId", "");
            m.getAttributes().put("emailDoc:from", "");
            m.getAttributes().put("emailDoc:subject", "");
            m.getAttributes().put("emailDoc:toList", "");
            m.getAttributes().put("emailDoc:ccList", "");
            m.getAttributes().put("emailDoc:bccList", "");
            m.getAttributes().put("emailDoc:references", "");
            m.getAttributes().put("emailDoc:relatedReferenceId", "");
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
