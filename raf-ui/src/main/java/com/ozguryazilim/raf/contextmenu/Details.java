package com.ozguryazilim.raf.contextmenu;

import com.ozguryazilim.raf.action.DetailsAction;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractContextMenuItem;
import com.ozguryazilim.raf.ui.base.ContextMenu;
import com.ozguryazilim.raf.util.RafContextUtils;

import javax.inject.Inject;

@ContextMenu(order = 10)
public class Details extends AbstractContextMenuItem {

    @Inject
    private DetailsAction action;

    @Override
    public boolean disabled(RafObject object) {
        return object == null;
    }

    @Override
    public boolean applicable() {
        return action.applicable(true);
    }

    @Override
    public void execute(RafObject object) {
        RafContextUtils.executeActionWithTempSelectedItemsIfNotSelected(object, context, action);
    }


}
