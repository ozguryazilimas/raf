/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.ui;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@SessionScoped
@Named
public class StartProcessDialog implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(StartProcessDialog.class);
    
    @Inject
    private ProcessService processService;
    
    @Inject
    private RuntimeDataService dataService;
    
    @Inject
    private DefinitionService bpmnDefinitionService;

    @Inject
    private RafContext context;
    
    @Inject
    private Identity identity;
    
    private String deploymentId;
    private String processId;
    
    private Map<String,Object> data = new HashMap<>();
    
    private List<RafObject> selectedRafItems = new ArrayList<>();
    
    public void openDialog( String deploymentId, String processId){
        
        this.deploymentId = deploymentId;
        this.processId = processId;
        this.data.clear();
        this.selectedRafItems.clear();
        
        ProcessDefinition processDesc = dataService.getProcessesByDeploymentIdProcessId("com.ozguryazilim.mutfak:raf-jbpm-sample-kjar:1.0.0-SNAPSHOT", "DocumentApproveProcess");
        Map<String, String> processData = bpmnDefinitionService.getProcessVariables("com.ozguryazilim.mutfak:raf-jbpm-sample-kjar:1.0.0-SNAPSHOT", "DocumentApproveProcess");
        
        if (processData == null) {
            processData = new HashMap<String, String>();
        }
        
        LOG.info("Process Form Contexts {} {}", processDesc, processData);
        
        Map<String, Object> options = new HashMap<>();
        RequestContext.getCurrentInstance().openDialog(getDialogId(), options, null);
    }
    
    /**
     * Process için gereken bilgiler alındı ve başlatılacak.
     */
    public void closeDialog(){
        
        LOG.info("Process Data : {}", data);

        //Belgeleri de parametre olarak ekleyelim.
        serializeRafObjects();
        
        data.put("initiator", identity.getLoginName());
        
        long processInstanceId = processService.startProcess(deploymentId, processId, data);

        String message = "processInstanceId =  " + processInstanceId;
        LOG.info(message);
        
        FacesMessages.info("Süreç başarı ile başlatıldı"); //FIXME: i18n
        
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
    public void cancelDialog(){
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
    public String getDialogId(){
        return "/bpm/processDialog";
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public List<RafObject> getSelectedDocuments(){
        if( selectedRafItems.isEmpty() ){
            //FIXME: Burada ya klasörler hiç alınmayacak ya da RafBinder/RafDosier tadında bir klasör alınacak.
            for( RafObject o : context.getSeletedItems() ){
                selectedRafItems.add(o);
            }
        }
        
        return selectedRafItems;
    }
    
    /**
     * Seçili olan RafObject'lerin id'lerini bir liste olarak yazacağız.
     */
    protected void serializeRafObjects(){
        
        List<String> rafOIDs = new ArrayList<>();
        for( RafObject o : selectedRafItems ){
            rafOIDs.add(o.getId());
        }
        
        data.put("document", rafOIDs);
    }
    
}
