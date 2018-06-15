/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base.metadatapanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.MetadataPanelPages;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import com.ozguryazilim.telve.messages.FacesMessages;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Her hangi bir belge için temel bilgiler.
 * 
 * RafObject üzerinden alınır.
 * 
 * FIXME: edit süreci ile ilgili yapılacak şeyler var.
 * 
 * @author Hakan Uygun
 */
@MetadataPanel(type = "nt:file", view = MetadataPanelPages.BasicMetadataPanel.class, editor = MetadataPanelPages.BasicMetadataEditorDialog.class, order = 0)
public class BasicMetadataPanel extends AbstractMetadataPanel{

    private static final Logger LOG = LoggerFactory.getLogger(BasicMetadataPanel.class);
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafContext context;
    
    private RafCategory category;

    @Override
    protected void initEditModel() {
        //FIXME: Burada servisten nesneyi bulacağız.
        category = null;
    }
    
    @Override
    protected void save() {
        try {
            
            LOG.info("Selected Category : {}", category);
            if( category != null ){
                context.getSelectedObject().setCategory(category.getName());
                LOG.info("Selected Category : {}", category.getName());
                //TODO: category attribute atanacak
            } else {
                //Modelde category varsa boşaltılacak
                context.getSelectedObject().setCategory("");
            }
            
            //FIXME: yetki kontrolü nerede yapılacak?
            rafService.saveProperties(context.getSelectedObject());
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Properties cannot saved", ex);
            FacesMessages.error("Properties cannot saved");
        }
    }

    public RafCategory getCategory() {
        return category;
    }

    public void setCategory(RafCategory category) {
        this.category = category;
    }
    
}
