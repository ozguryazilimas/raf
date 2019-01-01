package com.ozguryazilim.raf.jbpm.actions;

import com.ozguryazilim.raf.ui.base.AbstractAction;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME: süreç başlatma işine daha sonradan tekrar bakılacak. 
 * 
 * Sanırım bu actiona ihtiyaç olmayacak!
 * 
 * @author oyas
 */
/*
@Action(icon = "fa-paste",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.NeedClipboard},
        includedMimeType = "raf/folder",
        group = 10,
        order = 3)
*/
public class StartProcessAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(StartProcessAction.class);

    @Inject
    private DeploymentService deploymentService;
    
    @Inject
    private ProcessService processService;
    
    @Inject
    private RuntimeDataService dataService;
    
    @Inject
    private DefinitionService bpmnDefinitionService;

    @Override
    protected boolean finalizeAction() {
        LOG.info("Start Process");

        String taskActor = "telve";

        // start new process instance
        Map<String, Object> params = new HashMap<String, Object>();
        //params.put("taskActor", taskActor);
        //params.put("reason", "Yearly performance evaluation");
        
        params.put("approver", taskActor);
        params.put("initiator", taskActor);
        
        params.put("subject", "Belge onay ihtiyacı");
        params.put("description", "Belgenin onaya ihtiyacı var.");
        
        for( DeployedUnit du : deploymentService.getDeployedUnits()){
            for( DeployedAsset da : du.getDeployedAssets()){
                if( "PROCESS".equals( da.getKnowledgeType())){
                    LOG.info("Selectable Process {} {}", du.getDeploymentUnit().getIdentifier(), da.getId());
                }
            }
        }
        
        
        ProcessDefinition processDesc = dataService.getProcessesByDeploymentIdProcessId("com.ozguryazilim.mutfak:raf-jbpm-sample-kjar:1.0.0-SNAPSHOT", "DocumentApproveProcess");
        Map<String, String> processData = bpmnDefinitionService.getProcessVariables("com.ozguryazilim.mutfak:raf-jbpm-sample-kjar:1.0.0-SNAPSHOT", "DocumentApproveProcess");
        
        if (processData == null) {
            processData = new HashMap<String, String>();
        }
        
        LOG.info("Process Form Contexts {} {}", processDesc, processData);
        

        long processInstanceId = processService.startProcess("com.ozguryazilim.mutfak:raf-jbpm-sample-kjar:1.0.0-SNAPSHOT", "DocumentApproveProcess", params);
        //processService.setProcessVariables(processInstanceId, params);

        String message = "processInstanceId =  " + processInstanceId;
        LOG.info(message);

        return true;
    }
}
