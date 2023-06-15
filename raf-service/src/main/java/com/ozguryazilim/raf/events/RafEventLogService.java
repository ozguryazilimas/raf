package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafEventLog;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafEventLogService {
    
    @Inject
    private RafEventLogRepository logRepository;
    
    @Inject
    private RafDefinitionService rafDefinitionService;

    public void addEventLog( RafEventLog event ){
        logRepository.saveAndFlush(event);
    }

    public List<RafEventLog> getEventLogByUser( String username, String rafFilter, String typeFilter){
        List<RafDefinition> defs = rafDefinitionService.getRafsForUser(username);
        List<String> paths = new ArrayList<>();

        if (rafFilter == null || "*".equals(rafFilter)) {
            for( RafDefinition def : defs ){
                paths.add("/RAF/" + def.getCode() + "/%");
            }

            //Ortak alana konanlar herkese listelensin.
            //TODO: Bunu kullanıcı seçimine versek mi?
            paths.add("/SHARED/%" );
            //Repository'den şu anda max 10 adet dönüyor
        } else {
            paths.add(rafFilter + "%");
        }

        return !"*".equals(typeFilter) ?
                logRepository.findByPathsAndType(username, paths, typeFilter) :
                logRepository.findByPaths(username, paths);
    }

    public List<RafEventLog> getRecentlyEventsByUser(String username){
        //Repository'den şu anda max 10 adet dönüyor
        return logRepository.findRecentlyEventsByUsername(username);
    }

    public List<String> getEventLogTypes() {
        return logRepository.findCurrentEventTypes();
    }
}
