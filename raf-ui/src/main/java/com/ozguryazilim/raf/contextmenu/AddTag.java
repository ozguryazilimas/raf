package com.ozguryazilim.raf.contextmenu;

import com.ozguryazilim.raf.action.AddTagAction;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractContextMenuItem;
import com.ozguryazilim.raf.ui.base.ContextMenu;

import javax.inject.Inject;

@ContextMenu(order = 9)
public class AddTag extends AbstractContextMenuItem {

    @Inject
    private AddTagAction action;

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
        context.getSeletedItems().add(object);
        action.execute();
        context.getSeletedItems().remove(object);
    }


}
