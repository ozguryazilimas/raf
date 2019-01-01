package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.ui.base.AbstractRafCollectionCompactViewController;
import com.ozguryazilim.raf.ui.base.CollectionContentViewPanel;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Raf ekranında RafCollection gösterme paneli.
 * 
 * İçerik değişimini RafCollectionChangeEvent event üzerinden yakalayıp, detayları değiştiriyor.
 * 
 * @author Hakan Uygun
*/
@ContentPanel(actionIcon = "fa-list-ul", icon = "fa-folder-open" )
public class RafCollectionCompactViewPanel extends AbstractRafCollectionCompactViewController implements CollectionContentViewPanel{

    @Inject
    private RafContext context;
    
    
    public void listener(@Observes RafCollectionChangeEvent event) {
        clear();
        setCollection(context.getCollection());
    }

    
    
}
