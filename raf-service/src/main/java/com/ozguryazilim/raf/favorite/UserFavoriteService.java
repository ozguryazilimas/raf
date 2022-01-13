package com.ozguryazilim.raf.favorite;

import com.ozguryazilim.raf.entities.UserFavorite;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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

    public boolean isAddedFavorites(String username, String path) {
        return repository.findByUsernameAndPath(username, path).size() > 0;
    }

}
