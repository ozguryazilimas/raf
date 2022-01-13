package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.entities.UserFavorite;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class LazyUserFavoriteDataModel extends LazyDataModel<UserFavorite> {

    private final FavoritesDashlet favoritesDashlet;

    public LazyUserFavoriteDataModel(FavoritesDashlet favoritesDashlet) {
        this.favoritesDashlet = favoritesDashlet;
    }

    @Override
    public List<UserFavorite> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<UserFavorite> result = favoritesDashlet.getFavorites(first, pageSize);
        this.setRowCount(favoritesDashlet.getTotalCount());
        return result;
    }

}
