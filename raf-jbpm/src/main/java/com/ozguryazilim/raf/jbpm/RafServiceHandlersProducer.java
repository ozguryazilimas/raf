/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm;

import com.ozguryazilim.raf.jbpm.workitemhandler.RafServiceTaskHandler;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.runtime.manager.WorkItemHandlerProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafServiceHandlersProducer implements WorkItemHandlerProducer{

    private static final Logger LOG = LoggerFactory.getLogger(RafServiceHandlersProducer.class);
    
    @Override
    public Map<String, WorkItemHandler> getWorkItemHandlers(String identifier, Map<String, Object> params) {
        
        LOG.debug("Work Item Registery {}, {}", identifier, params);
        
        Map<String, WorkItemHandler> result = new HashMap<>();
        
        result.put("Raf Service Task", new RafServiceTaskHandler());
        
        return result;
        
    }
    
}
