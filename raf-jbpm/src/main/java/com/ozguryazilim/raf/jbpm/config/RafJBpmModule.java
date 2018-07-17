/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.config;

import com.ozguryazilim.raf.jbpm.identity.TelveUserGroupCallback;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.jbpm.kie.services.api.FormProviderService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafJBpmModule {
    
    private static final Logger LOG = LoggerFactory.getLogger(RafJBpmModule.class);
    private static final String KJAR_ID = "com.ozguryazilim.mutfak:raf-jbpm-sample-kjar:1.0.0-SNAPSHOT";
    
    @Inject
    DeploymentService deploymentService;
    
    @Inject
    FormProviderService formProviderService;
    
    @PostConstruct @Transactional
    public void init(){
        System.setProperty("org.jbpm.ht.callback", "custom");
        System.setProperty("org.jbpm.ht.custom.callback", TelveUserGroupCallback.class.getName());
        
        LOG.warn("KJar Yüklenecek");
        
        String[] gav = KJAR_ID.split(":");
        DeploymentUnit deploymentUnit = new KModuleDeploymentUnit(gav[0], gav[1], gav[2]);
        deploymentService.deploy(deploymentUnit);
        
        for( DeployedUnit du : deploymentService.getDeployedUnits()){
            LOG.info( "Deployed Unit : {}", du.getDeploymentUnit().getIdentifier());
            LOG.info( "Deployed Assests : {}", du.getDeployedAssets());
            LOG.info( "Deployed Classes : {}", du.getDeployedClasses());
            
            //LOG.info( "Deployed Form : {}", formProviderService.getFormDisplayProcess(du.getDeploymentUnit().getIdentifier(), "DocumentApproveProcess"));
        }
        
        LOG.warn("KJar Yüklendi");
    }
    
}
