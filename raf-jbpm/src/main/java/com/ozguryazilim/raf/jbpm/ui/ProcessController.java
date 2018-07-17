/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.DeployedUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Process'ler ve UI arasında temel kontrol işlemlerinin yapılmasını sağlar.
 * 
 * Sistemde kullanıcının erişebileceği süreçler ve bunların uygunluk durumunu belirleyerek Menu oluşturulmasını sağlar.
 * Kullanıcıdan gelen süreç başlatma isteğini karşılayıp ilgili UI açma işlemlerini yapar.
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class ProcessController implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);

    private Map<String,String> processMap = new HashMap<>();
    
    @Inject
    private DeploymentService deploymentService;
    
    @Inject
    private StartProcessDialog processDialog;
    
    public List<String> getProcesses(){
        //TODO: Süreçler ile ilgili kullanıcı yetkilendirmesi nasıl olacak? 
        
        List<String> result = new ArrayList<>();
        
        if( processMap.isEmpty()){
            populateProcesses();
        }
        
        result.addAll(processMap.keySet());
        
        return result;
    }
    
    public void startProcess( String processId ){
        LOG.info("Selected Process {} {}", processMap.get(processId), processId);
        processDialog.openDialog(processMap.get(processId), processId);
    }


    private void populateProcesses(){
        processMap.clear();
        
        for( DeployedUnit du : deploymentService.getDeployedUnits()){
            for( DeployedAsset da : du.getDeployedAssets()){
                if( "PROCESS".equals( da.getKnowledgeType())){
                    LOG.info("Selectable Process {} {}", du.getDeploymentUnit().getIdentifier(), da.getId());
                    processMap.put(da.getId(), du.getDeploymentUnit().getIdentifier());
                }
            }
        }
    }
}
