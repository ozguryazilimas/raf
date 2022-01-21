package com.ozguryazilim.raf.contextmenu;

import com.ozguryazilim.raf.action.DeleteAction;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractContextMenuItem;
import com.ozguryazilim.raf.ui.base.ContextMenu;

import javax.inject.Inject;

@ContextMenu(order = 8)
public class Delete extends AbstractContextMenuItem {

    @Inject
    private DeleteAction action;

    @Override
    public boolean applicable() {
        return action.applicable(true);
    }

    @Override
    public void execute(RafObject object) {
        context.getSeletedItems().add(object);
        action.execute();
        context.getSeletedItems().clear();
    }

}