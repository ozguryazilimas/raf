/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.telve.sequence.SequenceManager;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class RafRecordNoServiceHandler implements WorkItemHandler{
    
    private static final Logger LOG = LoggerFactory.getLogger(RafRecordNoServiceHandler.class);
    
    @Inject
    private SequenceManager sequenceManager;
    
    
    
    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters()); 
        
        Map<String,Object> metadata = (Map<String,Object>) workItem.getParameter("metadata");

        //FIXME: SERİ başlangıç ve stratejilerini düzenlemek lazım. Ama bunun için ek bilgi lazım
        String no = sequenceManager.getNewSerialNumber("ABC", 6);
        metadata.put("raf:recordNo", no);
        
        Map<String,Object> result = new HashMap<>();
        result.put("metadata", metadata);
        
        manager.completeWorkItem(workItem.getId(), result);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //NŞA'da abort olmamalı!
        LOG.warn("Work Abort!");
    }
}
