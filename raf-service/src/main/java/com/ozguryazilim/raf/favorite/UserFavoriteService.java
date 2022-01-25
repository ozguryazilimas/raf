package com.ozguryazilim.raf.favorite;

import com.ozguryazilim.raf.entities.UserFavorite;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserFavoriteService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(UserFavoriteService.class);

    @Inject
    private UserFavoriteRepository repository;
    
    public boolean addFavorites(String username, String path) {
        try {
            UserFavorite fav = new UserFavorite();
            fav.setUsername(username);
            fav.setPath(path);
            fav.setDate(new Date());
            repository.saveAndFlush(fav);
            return true;
        } catch (Exception ex) {
            LOG.error("Unknown Exception:", ex);
            return false;
        }
    }

    @Transactional
    public boolean removeFromFavorites(String username, String path) {
        try {

            repository.removeByUsernameAndPath(username, path);
            return true;
        } catch (Exception ex) {
            LOG.error("Unknown Exception:", ex);
            return false;
        }
    }

    public List<UserFavorite> getFavoritesByUser(String username) {
        return repository.findByUsername(username);
    }

    public List<String> getFavoriteRafPathsByUser(String username) {
        List<UserFavorite> favorites = getFavoritesByUser(username);

        if (favorites == null || favorites.isEmpty()) {
            return new ArrayList<>();
        }
        return favorites.stream()
                .map(UserFavorite::getPath)
                .filter(path -> path.split("/").length == 3)
                .collect(Collectors.toList());
    }

    public List<UserFavorite> getFavoritesByPath(String path){
        return repository.findByPath(path);
    }

    public boolean isAddedFavorites(String username, String path) {
        return repository.findByUsernameAndPath(username, path).size() > 0;
    }

}
