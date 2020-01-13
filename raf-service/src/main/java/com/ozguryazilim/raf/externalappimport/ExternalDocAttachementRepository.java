package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDocAttachement;
import com.ozguryazilim.telve.data.RepositoryBase;
import java.util.List;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 *
 * @author oyas
 */
@Repository
@Dependent
public abstract class ExternalDocAttachementRepository extends RepositoryBase<ExternalDocAttachement, ExternalDocAttachement> implements CriteriaSupport<ExternalDocAttachement> {

    public abstract List<ExternalDocAttachement> findByParentRafFilePathLike(String parentRafFilePath);

    public abstract void removeByParentRafFilePath(String parentRafFilePath);

    public abstract List<ExternalDocAttachement> findByRafFileId(String rafFileId);

    public abstract void removeByRafFileId(String rafFileId);

    public abstract List<ExternalDocAttachement> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);

    public abstract void removeByParentRafFilePathLike(String rafFilePath);

}
