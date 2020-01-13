package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDoc;
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
public abstract class ExternalDocRepository extends RepositoryBase<ExternalDoc, ExternalDoc> implements CriteriaSupport<ExternalDoc> {

    public abstract List<ExternalDoc> findByDocumentId(String documentId);

    public abstract List<ExternalDoc> findByRafFilePath(String rafFilePath);

    public abstract List<ExternalDoc> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByDocumentId(String documentId);

    public abstract void removeByRafFilePath(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);

}
