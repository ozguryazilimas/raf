package com.ozguryazilim.raf.contextmenu;

import com.ozguryazilim.raf.action.DownloadAction;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractContextMenuItem;
import com.ozguryazilim.raf.ui.base.ContextMenu;

import javax.inject.Inject;

@ContextMenu(order = 6, supportAjax = false)
public class Download extends AbstractContextMenuItem {

    @Inject
    private DownloadAction action;

    @Override
    public boolean disabled(RafObject object) {
        return object == null || object instanceof RafFolder;
    }

    @Override
    public void execute(RafObject object) {
        RafObject current = context.getSelectedObject();
        context.setSelectedObject(object);
        action.execute();
        context.setSelectedObject(current);
    }

}