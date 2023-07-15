package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.UserFavorite;
import com.ozguryazilim.raf.events.FavoritesChangedEvent;
import com.ozguryazilim.raf.favorite.UserFavoriteService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import org.primefaces.model.LazyDataModel;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canRefresh}, permission = "public")
public class FavoritesDashlet extends AbstractDashlet {

    private static final String FAVORITES_DASHLET_PAGINATION_LIMIT = "favorites.dashlet.pagination.limit";

    @Inject
    private Identity identity;

    @Inject
    private RafService rafService;

    @Inject
    private UserFavoriteService favoriteService;

    @Inject
    @UserAware
    private Kahve kahve;

    private List<UserFavorite> favorites;

    private LazyDataModel<UserFavorite> lazyFavorites;

    private Integer size;

    @Override
    public void load() {
        super.load();
        size = getPaginationLimit();
        favorites = getUserFavorites();
        lazyFavorites = new LazyUserFavoriteDataModel(this);
    }

    @Override
    public void refresh() {
        load();
    }

    public void removeFromFavorites(UserFavorite fav) {
        favoriteService.removeFromFavorites(fav.getUsername(), fav.getPath());
        favorites.remove(fav);
    }

    public boolean hasPreview(RafObject obj) {
        return obj instanceof RafDocument && ((RafDocument) obj).getHasPreview();
    }

    private List<UserFavorite> getUserFavorites() {
        return favoriteService.getFavoritesByUser(identity.getLoginName()).stream()
                .map(f -> {
                    try {
                        f.setObject(rafService.getRafObjectByPath(f.getPath()));
                    } catch (RafException e) {
                        // Verilen path'i raf objesine çeviremedi bu yüzden es geçip favorilerden kaldırıyoruz
                        // (Belki favorilenen dosya silindi.)
                        favoriteService.removeFromFavorites(identity.getLoginName(), f.getPath());
                        return null;
                    }
                    return f;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(UserFavorite::getDate).reversed())//Tarihe göre yeniden eskiye sıraladık.
                .collect(Collectors.toList());
    }

    public Integer getTotalCount() {
        return favorites.size();
    }

    private Integer getPaginationLimit() {
        return kahve.get(FAVORITES_DASHLET_PAGINATION_LIMIT, 4).getAsInteger();
    }

    public List<UserFavorite> getFavorites(int first, int pageSize) {
        //User yeni bir limit seçmiş mi?
        Integer oldSize = getPaginationLimit();
        if (oldSize != pageSize) {
            kahve.put(FAVORITES_DASHLET_PAGINATION_LIMIT, pageSize);
        }
        return favorites.stream()
                .skip(first)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public LazyDataModel<UserFavorite> getLazyFavorites() {
        return lazyFavorites;
    }

    public void setLazyFavorites(LazyDataModel<UserFavorite> lazyFavorites) {
        this.lazyFavorites = lazyFavorites;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void favoritesChangedEventListener(@Observes(notifyObserver = Reception.ALWAYS) FavoritesChangedEvent favoritesChangedEvent) {
        refresh();
    }

}