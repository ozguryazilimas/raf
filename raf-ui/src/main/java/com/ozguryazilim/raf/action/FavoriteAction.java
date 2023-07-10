package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafController;
import com.ozguryazilim.raf.events.FavoritesChangedEvent;
import com.ozguryazilim.raf.favorite.UserFavoriteService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.raf.ui.base.CollectionContentViewPanel;
import com.ozguryazilim.raf.ui.base.ObjectContentViewPanel;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Action(icon = "fa-heart",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.ChangeableStateIcon, ActionCapability.NeedSelection, ActionCapability.MultiSelection}
)
public class FavoriteAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteAction.class);

    @Inject
    private Identity identity;

    @Inject
    private UserFavoriteService service;

    @Inject
    private RafController rafController;

    @Inject
    private Event<FavoritesChangedEvent> favoritesChangedEventEvent;

    @Override
    protected boolean finalizeAction() {
        String username = getUsername();
        List<String> paths = getPaths();

        boolean isSucceed = true;

        List<String> nonFavoritedPaths = paths.stream()
                .filter(path -> !service.isAddedFavorites(username, path))
                .collect(Collectors.toList());

        if (nonFavoritedPaths.isEmpty()) {
            for (String path : paths) {
                isSucceed = service.removeFromFavorites(username, path);
            }
        } else {
            for (String path : nonFavoritedPaths) {
                isSucceed = service.addFavorites(username, path);
            }
        }

        // Did the operation complete successfully?
        if (!isSucceed) {
            LOG.error("[RAF-0047] An unexpected error occurred while adding/removing to favourites.");
            FacesMessages.error("[RAF-0047] An unexpected error occurred while adding/removing to favourites.");
        }

        favoritesChangedEventEvent.fire(new FavoritesChangedEvent());
        return super.finalizeAction();
    }

    @Override
    public String getChangeableStateIcon() {
        String username = getUsername();
        List<String> paths = getPaths();
        boolean isPathsAddedToFavorites = paths.stream()
                .allMatch(objectPath -> service.isAddedFavorites(username, objectPath));

        if (isPathsAddedToFavorites && !CollectionUtils.isEmpty(paths)) {
            return this.getIcon() + " icon-red";
        } else {
            return this.getIcon();
        }
    }

    public void removeFromFavorites(String username, String path) {
        service.removeFromFavorites(username, path);
    }

    private String getUsername() {
        return identity.getLoginName();
    }

    private List<String> getPaths() {
        List<String> paths = new ArrayList<>();

        if (rafController.getSelectedContentPanel() instanceof CollectionContentViewPanel) {
            paths = getContext().getSeletedItems().stream()
                    .map(RafObject::getPath)
                    .collect(Collectors.toList());
        }
        else if (rafController.getSelectedContentPanel() instanceof ObjectContentViewPanel) {
            if (getContext().getSelectedObject() != null) {
                paths.add(getContext().getSelectedObject().getPath());
            } else if (getContext().getSelectedRaf() != null && getContext().getSelectedRaf().getNode() != null) {
                paths.add(getContext().getSelectedRaf().getNode().getPath());
            } else {
                LOG.error("[RAF-0046] Path definition not found.");
                FacesMessages.error("[RAF-0046] Path definition not found.");
            }
        }


        return paths;
    }

}
