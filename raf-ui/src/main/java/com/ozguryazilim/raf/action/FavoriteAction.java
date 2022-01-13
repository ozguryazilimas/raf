package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.favorite.UserFavoriteService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Action(icon = "fa-heart",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.ChangeableStateIcon}
)
public class FavoriteAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteAction.class);

    @Inject
    private Identity identity;

    @Inject
    private UserFavoriteService service;

    @Override
    protected boolean finalizeAction() {
        String username = getUsername();
        String path = getPath();

        boolean isSucceed;
        if (service.isAddedFavorites(username, path)) {
            isSucceed = service.removeFromFavorites(username, path);
        } else {
            isSucceed = service.addFavorites(username, path);
        }

        // Did the operation complete successfully?
        if (!isSucceed) {
            LOG.error("[RAF-0047] An unexpected error occurred while adding/removing to favourites.");
            FacesMessages.error("[RAF-0047] An unexpected error occurred while adding/removing to favourites.");
        }

        return super.finalizeAction();
    }

    @Override
    public String getChangeableStateIcon() {
        String username = getUsername();
        String path = getPath();
        if (service.isAddedFavorites(username, path)) {
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

    private String getPath() {
        String path = null;
        if (getContext().getSelectedObject() != null) {
            path = getContext().getSelectedObject().getPath();
        } else if (getContext().getSelectedRaf() != null && getContext().getSelectedRaf().getNode() != null) {
            path = getContext().getSelectedRaf().getNode().getPath();
        } else {
            LOG.error("[RAF-0046] Path definition not found.");
            FacesMessages.error("[RAF-0046] Path definition not found.");
        }
        return path;
    }

}
