package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafController;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.ui.base.AbstractRafCollectionTableViewController;
import com.ozguryazilim.raf.ui.base.CollectionContentViewPanel;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import org.apache.deltaspike.core.util.ProxyUtils;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ContentPanel(actionIcon = "fa-align-justify", icon = "fa-folder-open" )
public class RafCollectionTableViewPanel extends AbstractRafCollectionTableViewController implements CollectionContentViewPanel{
    
    @Inject
    private RafContext context;

    @Inject
    private RafController rafController;

    public void listener(@Observes RafCollectionChangeEvent event) {
        if (ProxyUtils.getUnproxiedClass(rafController.getSelectedContentPanel().getClass()).equals(this.getClass())) {
            clear();
            setCollection(context.getCollection());
        }
    }
    
}
