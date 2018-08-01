/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafEventLog;
import java.util.ArrayList;
import java.util.List;
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
        List<String> paths = new ArrayList<>();
        
        for( RafDefinition def : defs ){
            paths.add("/RAF/" + def.getCode() + "/%");
        }
        
        //Ortak alana konanlar herkese listelensin. 
        //TODO: Bunu kullanıcı seçimine versek mi?
        paths.add("/SHARED/%" );
        
        return logRepository.findByPaths(username, paths);
    }
    
}
