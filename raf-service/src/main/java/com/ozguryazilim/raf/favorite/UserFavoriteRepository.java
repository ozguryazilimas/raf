package com.ozguryazilim.raf.favorite;

import com.ozguryazilim.raf.entities.UserFavorite;
import com.ozguryazilim.telve.data.RepositoryBase;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import javax.enterprise.context.Dependent;
import java.util.List;

@Repository
@Dependent
public abstract class UserFavoriteRepository extends RepositoryBase<UserFavorite, UserFavorite> implements CriteriaSupport<UserFavorite> {

    public abstract List<UserFavorite> findByUsername(String username);

    public abstract List<UserFavorite> findByPath(String path);

    public abstract List<UserFavorite> findByUsernameAndPath(String username, String path);

    public abstract void removeByUsernameAndPath(String username, String path);

}
