package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.entities.RafEventLog_;
import com.ozguryazilim.telve.data.RepositoryBase;
import java.util.ArrayList;
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
    
    public List<RafEventLog> findByPaths( String username, List<String> paths ){
        Criteria<RafEventLog,RafEventLog> crit = criteria();
        
        List<Criteria<RafEventLog,RafEventLog>> partCrits = new ArrayList<>();
        paths.forEach((p) -> {
            partCrits.add( criteria().like(RafEventLog_.path, p));
        });
        
        crit.or(partCrits);
        crit.orderDesc(RafEventLog_.logTime);
        //FIXME: burada limit ihtiyacımız var. Tüm eentleri döndürmeyelim.
        return crit.getResultList();
    }
    
}
