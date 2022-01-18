package com.ozguryazilim.raf.contextmenu;

import com.ozguryazilim.raf.action.PasteAction;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractContextMenuItem;
import com.ozguryazilim.raf.ui.base.ContextMenu;

import javax.inject.Inject;

@ContextMenu(order = 5)
public class Paste extends AbstractContextMenuItem {

    @Inject
    private PasteAction action;

    @Override
    public boolean disabled(RafObject object) {
        return context.getClipboard() == null || context.getClipboard().isEmpty() || object == null;
    }

    @Override
    public boolean applicable() {
        return action.applicable(true);
    }

    @Override
    public void execute(RafObject object) {
        RafObject current = context.getSelectedObject();
        context.setSelectedObject(object);
        action.execute();
        context.setSelectedObject(current);
    }

}