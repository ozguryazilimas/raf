/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class DocumentViewPanel extends RafObjectContentPanel{

    @Inject
    private RafContext context;
    
    //FIXME: bunlar bir registery'den plugin yapısı ile alınacaklar.
    @Inject
    private DefaultDocumentPreview defaultPreview;
    
    @Inject
    private ImagePreview imagePreview;
    
    @Inject
    private PdfPreview pdfPreview;
    
    @Override
    public String getFragment() {
        return "/fragments/documentView.xhtml";
    }

    @Override
    public String getCommandIcon() {
        return "fa-file";
    }

    @Override
    public String getCommandTitle() {
        return "Detail View";
    }
    
    
    /**
     * Geriye mimeType'a göre hangi widget kullanılacak ise onun fragman bilgisini döner.
     * 
     * @return 
     */
    public String getPreviewWidget(){
        
        if( context.getSelectedObject().getMimeType().startsWith("image/") ){
            return imagePreview.getFragment();
        }
        
        if( context.getSelectedObject().getMimeType().startsWith("application/pdf") ){
            return pdfPreview.getFragment();
        }
        
        return defaultPreview.getFragment();
    } 
    
}
