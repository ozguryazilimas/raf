/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base.metadatapanels;

import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.MetadataPanelPages;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import javax.inject.Inject;

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

    @Inject
    private RafService rafService;
    
    @Override
    protected void save() {
        //FIXME: değerler nasıl saklanacak?
        //FIXME: yetki kontrolü nerede yapılacak?
        //rafService.updateMetadata();
    }
 
    
}
