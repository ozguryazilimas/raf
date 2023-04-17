package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.AbstractRafObjectViewController;
import com.ozguryazilim.raf.ui.base.DocumentsWidgetController;
import com.ozguryazilim.raf.ui.base.PreviewPanelRegistery;
import com.ozguryazilim.raf.ui.base.metadatapanels.ShareMetadataPanel;

import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
public class AbstractRafRecordViewController extends AbstractRafObjectViewController<RafRecord> implements DocumentsWidgetController{
    
    @Inject
    private RecordMetadataPanel recordMetadataPanel;

    @Inject
    private ShareMetadataPanel shareMetadataPanel;

    @Override
    public String getViewId() {
        return "/fragments/recordView.xhtml";
    }

    @Override
    public List<? extends RafObject> getRafObjects() {
        return getObject().getDocuments();
    }

    @Override
    protected void addCustomMetadataPanel(List<AbstractMetadataPanel> list) {
        recordMetadataPanel.setObject(getObject());
        shareMetadataPanel.setObject(getObject());
        list.add(recordMetadataPanel);
        list.add(shareMetadataPanel);
    }
    
    
    
    /**
     * Geriye mimeType'a göre hangi widget kullanılacak ise onun fragman bilgisini döner.
     * 
     * @return 
     */
    public String getPreviewWidget(){
        //Eğer mimetype yoksa default isteyelim
        if( getMainDocument() != null ){
            return PreviewPanelRegistery.getMimeTypePanel(getMainDocument().getMimeType()).getViewId();
        } else {
            return PreviewPanelRegistery.getMimeTypePanel("default").getViewId();
        }
        
    }
    
    public RafObject getMainDocument(){
        for( RafDocument doc : getObject().getDocuments() ){
            if( doc.getName().equals(getObject().getMainDocument()) ){
                return doc;
            }
        }
        
        return null;
    }
}
