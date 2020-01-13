package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDocRelatedDoc;
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
public abstract class ExternalDocRelatedDocRepository extends RepositoryBase<ExternalDocRelatedDoc, ExternalDocRelatedDoc> implements CriteriaSupport<ExternalDocRelatedDoc> {

    public abstract List<ExternalDocRelatedDoc> findByParentRafFilePathLike(String parentRafFilePath);

    public abstract void removeByParentRafFilePath(String parentRafFilePath);

    public abstract List<ExternalDocRelatedDoc> findByRafFileId(String rafFileId);

    public abstract void removeByRafFileId(String rafFileId);

    public abstract List<ExternalDocRelatedDoc> findByParentRafFilePathAndRafFilePath(String parentRafFilePath, String rafFilePath);

    public abstract List<ExternalDocRelatedDoc> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);

    public abstract void removeByParentRafFilePathLike(String rafFilePath);

}
