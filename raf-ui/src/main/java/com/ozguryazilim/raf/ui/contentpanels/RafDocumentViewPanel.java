package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractRafDocumentViewController;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import com.ozguryazilim.raf.ui.base.ObjectContentViewPanel;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * RafDocument tipi nesneleri Raf Consolunda göstermek için ContentViewPanel
 * 
 * @author Hakan Uygun
 */
@ContentPanel( actionIcon = "fa-file")
public class RafDocumentViewPanel extends AbstractRafDocumentViewController implements ObjectContentViewPanel{
 
    @Inject
    private RafContext context;
    
    public void listener(@Observes RafCollectionChangeEvent event) {
        //setObject(context.getSelectedObject());
    }
    
    @Override
    public boolean acceptObject(RafObject object) {
        return object instanceof RafDocument;
    }
    
    @Override
    public void setRafObject(RafObject object) {
        setObject((RafDocument)object);
    }
}
