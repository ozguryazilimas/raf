/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.action.FileUploadAction;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafVersion;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
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
    
    @Inject
    private FileUploadAction fileUploadAction;
    
    @Inject 
    private RafService rafService;
    
    private List<RafVersion> versions = null;

    @Override
    public void setObject(RafDocument object) {
        super.setObject(object); 
        versions = null;
    }
    
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
    
    public void checkin(){
        fileUploadAction.execute("CHECKIN", getObject().getPath());
    }
    
    public List<RafVersion> getVersionHistory(){
        
        if( versions == null ){
            try {
                versions = rafService.getVersionHistory(getObject());
            } catch (RafException ex) {
                LOG.error("Raf Exception", ex);
                FacesMessages.error(ex.getMessage());
                versions = new ArrayList<>();
            }
        }
        
        return versions;
    }
    
}
