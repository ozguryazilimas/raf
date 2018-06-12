/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.DefaultDocumentPreview;
import com.ozguryazilim.raf.ImagePreview;
import com.ozguryazilim.raf.PdfPreview;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.config.ContentPanelPages;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import com.ozguryazilim.raf.ui.base.ObjectContentPanel;
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
    private DefaultDocumentPreview defaultPreview;
    
    @Inject
    private ImagePreview imagePreview;
    
    @Inject
    private PdfPreview pdfPreview;
    
    /**
     * Geriye mimeType'a göre hangi widget kullanılacak ise onun fragman bilgisini döner.
     * 
     * @return 
     */
    public String getPreviewWidget(){
        if( context.getSelectedObject() != null ){
            if( context.getSelectedObject().getMimeType().startsWith("image/") ){
                return imagePreview.getFragment();
            }

            if( context.getSelectedObject().getMimeType().startsWith("application/pdf") ){
                return pdfPreview.getFragment();
            }
        }
        
        return defaultPreview.getFragment();
    } 
    
}
