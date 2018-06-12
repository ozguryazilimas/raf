/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.invoice;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.invoice.config.InvoicePages;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.util.Date;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@MetadataPanel(type = "invoice:metadata", view = InvoicePages.InvoiceMetadataPanel.class, editor = InvoicePages.InvoiceMetadataEditor.class)
public class InvoiceMetadataPanel extends AbstractMetadataPanel {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceMetadataPanel.class);
    
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
            m.setType("invoice:metadata");
            m.getAttributes().put("invoice:date", new Date());
            m.getAttributes().put("invoice:account", "");
            m.getAttributes().put("invoice:serial", "");
            m.getAttributes().put("invoice:taxOffice", "");
            m.getAttributes().put("invoice:taxNumber", "");

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
