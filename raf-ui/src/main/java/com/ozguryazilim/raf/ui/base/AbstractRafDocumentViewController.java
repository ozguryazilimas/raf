/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.models.RafDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RafDocument view controlü için taban sınıf.
 * 
 * Üzerinde View Id, PreviewPanel ile ilgili kontroller bulunuyor.
 * 
 * @author Hakan Uygun
 */
public class AbstractRafDocumentViewController extends AbstractRafObjectViewController<RafDocument>{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRafDocumentViewController.class);
    
    @Override
    public String getViewId() {
        return "/fragments/documentView.xhtml";
    }
    
    /**
     * Geriye mimeType'a göre hangi widget kullanılacak ise onun fragman bilgisini döner.
     * 
     * @return 
     */
    public String getPreviewWidget(){
        //Eğer mimetype yoksa default isteyelim
        if( getObject() != null ){
            return PreviewPanelRegistery.getMimeTypePanel(getObject().getMimeType()).getViewId();
        } else {
            return PreviewPanelRegistery.getMimeTypePanel("default").getViewId();
        }
        
    }
    
}
