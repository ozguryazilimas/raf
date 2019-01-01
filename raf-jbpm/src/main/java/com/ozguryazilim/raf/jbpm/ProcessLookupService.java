package com.ozguryazilim.raf.jbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.DeployedUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProcessId ile deployment, title v.s. bulmak için servis.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class ProcessLookupService implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessLookupService.class);
    
    @Inject
    private DeploymentService deploymentService;
    
    private Map<String,String> processMap = new HashMap<>();
    private Map<String,String> processNameMap = new HashMap<>();
    
    @PostConstruct
    public void init(){
        populateProcesses();
    }
   
    public String getDeploymentId( String processId ){
        return processMap.get(processId);
    }
    
    public String getProcessName( String processId ){
        return processNameMap.get(processId);
    }
    
    
    private void populateProcesses(){
        processMap.clear();
        
        for( DeployedUnit du : deploymentService.getDeployedUnits()){
            for( DeployedAsset da : du.getDeployedAssets()){
                if( "PROCESS".equals( da.getKnowledgeType())){
                    LOG.info("Selectable Process {} {}", du.getDeploymentUnit().getIdentifier(), da.getId());
                    processMap.put(da.getId(), du.getDeploymentUnit().getIdentifier());
                    processNameMap.put(da.getId(), da.getName());
                }
            }
        }
    }
}
