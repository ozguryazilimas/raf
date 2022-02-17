package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.entities.RafEventLog_;
import com.ozguryazilim.telve.data.RepositoryBase;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.Criteria;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import javax.enterprise.context.Dependent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RafEventLog Repository
 * @author Hakan Uygun
 */
@Repository
@Dependent
public abstract class RafEventLogRepository extends RepositoryBase<RafEventLog, RafEventLog> implements CriteriaSupport<RafEventLog>{

    public List<RafEventLog> findRecentlyEventsByUsername(String username) {
        Criteria<RafEventLog,RafEventLog> crit = criteria()
                .like(RafEventLog_.username, username)
                .or(
                        criteria().eq(RafEventLog_.type, "DownloadDocument"),
                        criteria().eq(RafEventLog_.type, "UploadDocument")
                );

        crit.orderDesc(RafEventLog_.logTime);
        List<RafEventLog> recentDownloadAndUpload = crit.createQuery().setMaxResults(10).getResultList();
        List<RafEventLog> recentSelectDocument = findDistinctEventByUsername(username, "SelectDocument", 10);

        List<RafEventLog> recentEvents = new ArrayList<>();
        recentEvents.addAll(recentDownloadAndUpload);
        recentEvents.addAll(recentSelectDocument);

        return recentEvents.stream()
                .sorted(Comparator.comparing(RafEventLog::getLogTime).reversed())
                .limit(10)
                .collect(Collectors.toList());
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

    @Query(value = "SELECT * FROM raf_events WHERE id IN (SELECT MAX(id) FROM raf_events GROUP BY message) AND username = ?1 AND type= ?2 ORDER BY log_time ASC limit ?3", isNative = true)
    public abstract List<RafEventLog> findDistinctEventByUsername(String username, String eventType, int limit);

}
