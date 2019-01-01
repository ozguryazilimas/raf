package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractRafFolderViewController;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import com.ozguryazilim.raf.ui.base.ObjectContentViewPanel;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ContentPanel( actionIcon = "fa-folder")
public class RafFolderViewPanel extends AbstractRafFolderViewController implements ObjectContentViewPanel{
    
    @Inject
    private RafContext context;
    
    public void listener(@Observes RafCollectionChangeEvent event) {
        //setObject(context.getSelectedObject());
    }
    
    @Override
    public boolean acceptObject(RafObject object) {
        return object instanceof RafFolder;
    }
    
    @Override
    public void setRafObject(RafObject object) {
        setObject((RafFolder)object);
    }
}
