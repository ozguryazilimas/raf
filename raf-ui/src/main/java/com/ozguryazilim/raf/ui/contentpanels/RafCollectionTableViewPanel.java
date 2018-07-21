/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.ui.base.AbstractRafCollectionTableViewController;
import com.ozguryazilim.raf.ui.base.CollectionContentViewPanel;
import com.ozguryazilim.raf.ui.base.ContentPanel;
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
    
    public void listener(@Observes RafCollectionChangeEvent event) {
        clear();
        setCollection(context.getCollection());
    }
    
}
