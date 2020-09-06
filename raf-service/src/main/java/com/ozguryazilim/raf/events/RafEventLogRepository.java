package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.entities.RafEventLog_;
import com.ozguryazilim.telve.data.RepositoryBase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
 
    public abstract List<RafEventLog> findByUsername( String username);
    

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
        
        return crit.createQuery().setMaxResults(10).getResultList();
    }

    public List<RafEventLog> findByPathsAndDate(String username, List<String> paths, Date date) {
        Criteria<RafEventLog,RafEventLog> crit = criteria();

        List<Criteria<RafEventLog,RafEventLog>> partCrits = new ArrayList<>();
        paths.forEach((p) -> {
            partCrits.add( criteria().like(RafEventLog_.path, p));
        });

        crit.or(partCrits);
        crit.gtOrEq(RafEventLog_.logTime, date);
        crit.orderDesc(RafEventLog_.logTime);

        return crit.createQuery().setMaxResults(10).getResultList();
    }
}
