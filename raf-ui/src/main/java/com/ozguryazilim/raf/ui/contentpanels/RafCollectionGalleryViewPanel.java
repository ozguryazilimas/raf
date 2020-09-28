package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.ui.base.AbstractRafCollectionGalleryViewController;
import com.ozguryazilim.raf.ui.base.CollectionContentViewPanel;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ContentPanel(actionIcon = "fa-th", icon = "fa-folder-open")
public class RafCollectionGalleryViewPanel extends AbstractRafCollectionGalleryViewController implements CollectionContentViewPanel {

    @Inject
    private RafContext context;

    public void listener(@Observes RafCollectionChangeEvent event) {
        clear();
        setCollection(context.getCollection());
    }

}
