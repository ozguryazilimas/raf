/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.ui.previewpanels.DefaultPreviewPanel;
import com.ozguryazilim.raf.ui.previewpanels.ImagePreviewPanel;
import com.ozguryazilim.raf.ui.previewpanels.PdfPreviewPanel;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.config.ContentPanelPages;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import com.ozguryazilim.raf.ui.base.ObjectContentPanel;
import com.ozguryazilim.raf.ui.base.PreviewPanelRegistery;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ContentPanel( actionIcon = "fa-file", view = ContentPanelPages.DocumentViewPanel.class)
public class DocumentViewPanel extends ObjectContentPanel{

    @Inject
    private RafContext context;
    
    //FIXME: bunlar bir registery'den plugin yapısı ile alınacaklar.
    @Inject
    private DefaultPreviewPanel defaultPreview;
    
    @Inject
    private ImagePreviewPanel imagePreview;
    
    @Inject
    private PdfPreviewPanel pdfPreview;
    
    /**
     * Geriye mimeType'a göre hangi widget kullanılacak ise onun fragman bilgisini döner.
     * 
     * @return 
     */
    public String getPreviewWidget(){
        //Eğer mimetype yoksa default isteyelim
        if( context.getSelectedObject() != null ){
            return PreviewPanelRegistery.getMimeTypePanel(context.getSelectedObject().getMimeType()).getViewId();
        } else {
            return PreviewPanelRegistery.getMimeTypePanel("default").getViewId();
        }
        
    } 
    
}
