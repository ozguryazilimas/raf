package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDocWFStep;
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
public abstract class ExternalDocWFStepRepository extends RepositoryBase<ExternalDocWFStep, ExternalDocWFStep> implements CriteriaSupport<ExternalDocWFStep> {

    public abstract List<ExternalDocWFStep> findByDocumentWFId(String documentWFId);

    public abstract List<ExternalDocWFStep> findByDocumentWFIdAndStepName(String documentWFId, String stepName);

    public abstract List<ExternalDocWFStep> findByRafFilePath(String rafFilePath);

    public abstract List<ExternalDocWFStep> findByRafFilePathOrderByStartedDate(String rafFilePath);

    public abstract void removeByRafFilePath(String rafFilePath);

    public abstract List<ExternalDocWFStep> findByRafFileId(String rafFileId);

    public abstract void removeByRafFileId(String rafFileId);

    public abstract List<ExternalDocWFStep> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);

}
