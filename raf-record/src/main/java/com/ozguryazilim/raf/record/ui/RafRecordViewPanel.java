package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import com.ozguryazilim.raf.ui.base.ObjectContentViewPanel;
import java.util.Map;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ContentPanel(actionIcon = "fa-file")
public class RafRecordViewPanel extends AbstractRafRecordViewController implements ObjectContentViewPanel {

    @Inject
    private RafContext context;

    public void listener(@Observes RafCollectionChangeEvent event) {
        //setObject(context.getSelectedObject());
    }

    @Override
    public boolean acceptObject(RafObject object) {
        return object instanceof RafRecord;
    }

    @Override
    public void setRafObject(RafObject object) {
        setObject((RafRecord) object);      
    }

}
