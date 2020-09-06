package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafEventLog;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
    
    public List<RafEventLog> getEventLogByUser( String username){
        List<RafDefinition> defs = rafDefinitionService.getRafsForUser(username);
        return getEventLogByUserAndPaths(username, defs);
    }

    public List<RafEventLog> getEventLogByUserAndRaf(String username, RafDefinition rafDefinition) {
        return getEventLogByUserAndPaths(username, Collections.singletonList(rafDefinition));
    }

    public List<RafEventLog> getEventLogByUserAndPaths(String username, List<RafDefinition> rafs) {
        List<String> paths = rafs.stream().map(rafDefinition -> "/RAF/" + rafDefinition.getCode() + "/%").collect(Collectors.toList());
        paths.add("/SHARED/%");
        return logRepository.findByPaths(username, paths);
    }
}
