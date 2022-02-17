package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.entities.RafEventLog_;
import com.ozguryazilim.telve.data.RepositoryBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javax.enterprise.context.Dependent;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

/**
 * RafEventLog Repository
 * @author Hakan Uygun
 */
@Repository
@Dependent
public abstract class RafEventLogRepository extends RepositoryBase<RafEventLog, RafEventLog> implements CriteriaSupport<RafEventLog>{

    private List<String> recentlyEventTypes = Arrays.asList("SelectDocument", "DownloadDocument", "UploadDocument");

    public List<RafEventLog> findByUsername(String username) {
        Criteria<RafEventLog,RafEventLog> crit = criteria().like(RafEventLog_.username, username);
        List<Criteria<RafEventLog,RafEventLog>> partCrits = new ArrayList<>();
        recentlyEventTypes.forEach((p) -> {
            partCrits.add( criteria().like(RafEventLog_.type, p));
        });

        crit.or(partCrits);
        crit.orderDesc(RafEventLog_.logTime);

        return crit.createQuery().setMaxResults(10).getResultList();

    }


    /**
     * FIXME: Şu anda kafadan 10 ile sınırlandırıldı. Daha sonrası için düzenlenmesi gerek.
     * @param username
     * @param paths
     * @return 
     */
    public List<RafEventLog> findByPaths( String username, List<String> paths ){
        Criteria<RafEventLog,RafEventLog> crit = criteria();
        
        List<Criteria<RafEventLog,RafEventLog>> partCrits = new ArrayList<>();
        paths.forEach((p) -> {
            partCrits.add( criteria().like(RafEventLog_.path, p));
        });
        
        crit.or(partCrits);
        crit.orderDesc(RafEventLog_.logTime);
        crit.notLike(RafEventLog_.type, "SelectDocument");

        return crit.createQuery().setMaxResults(10).getResultList();
    }
    
}
