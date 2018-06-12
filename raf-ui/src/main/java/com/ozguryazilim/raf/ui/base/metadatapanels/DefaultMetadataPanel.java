/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base.metadatapanels;

import com.ozguryazilim.raf.config.MetadataPanelPages;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;

/**
 * Detayları bilinmeyen ( çoğu zaman sequencer ile toplanan ) metadataları göstermek için kullanılır.
 * 
 * 
 * @author Hakan Uygun
 */
@MetadataPanel(type = "*:metadata", view = MetadataPanelPages.DefaultMetadataPanel.class, order = 20)
public class DefaultMetadataPanel extends AbstractMetadataPanel{
    
}
