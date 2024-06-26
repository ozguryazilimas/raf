package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafController;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.ui.base.AbstractRafCollectionCompactViewController;
import com.ozguryazilim.raf.ui.base.CollectionContentViewPanel;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import org.apache.deltaspike.core.util.ProxyUtils;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Objects;

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

    @Inject
    private RafController rafController;

    public void listener(@Observes RafCollectionChangeEvent event) {
        if (Objects.equals(rafController.getSelectedCollectionContentPanel().getName(), this.getName())) {
            clear();
            setCollection(context.getCollection());
        }
    }

    
    
}
