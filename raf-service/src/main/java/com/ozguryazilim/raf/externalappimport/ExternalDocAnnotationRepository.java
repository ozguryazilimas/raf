package com.ozguryazilim.raf.externalappimport;

import com.ozguryazilim.raf.entities.ExternalDocAnnotation;
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
public abstract class ExternalDocAnnotationRepository extends RepositoryBase<ExternalDocAnnotation, ExternalDocAnnotation> implements CriteriaSupport<ExternalDocAnnotation> {

    public abstract List<ExternalDocAnnotation> findByRafFilePath(String rafFilePath);

    public abstract List<ExternalDocAnnotation> findByRafFileId(String rafFileId);

    public abstract void removeByRafFilePath(String rafFilePath);

    public abstract void removeByRafFileId(String rafFileId);

    public abstract List<ExternalDocAnnotation> findByRafFilePathLike(String rafFilePath);

    public abstract void removeByRafFilePathLike(String rafFilePath);

}
