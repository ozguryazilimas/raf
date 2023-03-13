package com.ozguryazilim.raf.contextmenu;

import com.ozguryazilim.raf.action.FavoriteAction;
import com.ozguryazilim.raf.favorite.UserFavoriteService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractContextMenuItem;
import com.ozguryazilim.raf.ui.base.ContextMenu;
import com.ozguryazilim.telve.auth.Identity;
import org.apache.shiro.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@ContextMenu(order = 8)
public class RemoveFavorites extends AbstractContextMenuItem {

    @Inject
    private FavoriteAction action;

    @Inject
    private Identity identity;

    @Inject
    private UserFavoriteService service;

    @Override
    public boolean disabled(RafObject object) {
        if (object != null && context.getSeletedItems().contains(object)) {
            return context.getSeletedItems().stream()
                    .noneMatch(selectedObject -> service.isAddedFavorites(identity.getLoginName(), selectedObject.getPath()));
        } else {
            return object == null || !service.isAddedFavorites(identity.getLoginName(), object.getPath());
        }
    }

    @Override
    public boolean applicable() {
        return action.applicable(true);
    }

    @Override
    public void execute(RafObject object) {
        if (context.getSeletedItems().contains(object)) {
            action.execute();
        } else {
            List<RafObject> oldSelectedItems = context.getSeletedItems();

            context.setSeletedItems(Collections.singletonList(object));
            action.execute();
            context.setSeletedItems(oldSelectedItems);
        }
    }
}
