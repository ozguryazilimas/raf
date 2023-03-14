package com.ozguryazilim.raf.contextmenu;

import com.ozguryazilim.raf.action.FavoriteAction;
import com.ozguryazilim.raf.favorite.UserFavoriteService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractContextMenuItem;
import com.ozguryazilim.raf.ui.base.ContextMenu;
import com.ozguryazilim.raf.util.RafContextUtils;
import com.ozguryazilim.telve.auth.Identity;

import javax.inject.Inject;

@ContextMenu(order = 7)
public class AddFavorites extends AbstractContextMenuItem {

    @Inject
    private FavoriteAction action;

    @Inject
    private Identity identity;

    @Inject
    private UserFavoriteService service;

    @Override
    public boolean disabled(RafObject object) {
        if (RafContextUtils.isSelected(object, context)) {
            return isAllSelectedItemsFavorited();
        } else {
            return object == null || service.isAddedFavorites(identity.getLoginName(), object.getPath());
        }
    }

    @Override
    public boolean applicable() {
        return action.applicable(true);
    }

    @Override
    public void execute(RafObject object) {
        RafContextUtils.executeActionWithTempSelectedItemsIfNotSelected(object, context, action);
    }

    private boolean isAllSelectedItemsFavorited() {
        return context.getSeletedItems().stream()
                .allMatch(selectedObject -> service.isAddedFavorites(identity.getLoginName(), selectedObject.getPath()));
    }

}