package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDocWF;
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
public abstract class ExternalDocWFRepository extends RepositoryBase<ExternalDocWF, ExternalDocWF> implements CriteriaSupport<ExternalDocWF> {

    public abstract List<ExternalDocWF> findByDocumentId(String documentId);

    public abstract List<ExternalDocWF> findByDocumentWFId(String documentWFId);

    public abstract List<ExternalDocWF> findByRafFilePath(String rafFilePath);

    public abstract void removeByDocumentId(String documentId);

    public abstract void removeByRafFilePath(String rafFilePath);

    public abstract List<ExternalDocWF> findByRafFileId(String rafFileId);

    public abstract void removeByRafFileId(String rafFileId);

    public abstract List<ExternalDocWF> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);

}
