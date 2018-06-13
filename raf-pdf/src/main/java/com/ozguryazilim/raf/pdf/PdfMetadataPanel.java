/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.pdf;

import com.ozguryazilim.raf.pdf.config.PdfPages;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;

/**
 *
 * @author oyas
 */
@MetadataPanel(type = "pdf:metadata", view = PdfPages.PdfMetadataPanel.class, order = 20)
public class PdfMetadataPanel extends AbstractMetadataPanel{
    
}
