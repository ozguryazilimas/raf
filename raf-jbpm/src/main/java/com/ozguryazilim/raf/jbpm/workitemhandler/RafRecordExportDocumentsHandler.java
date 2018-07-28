/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafRecord;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
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
public class RafRecordExportDocumentsHandler implements WorkItemHandler{
    
    private static final Logger LOG = LoggerFactory.getLogger(RafRecordExportDocumentsHandler.class);
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RuntimeDataService runtimeDataService;

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters());

        String recordObjectId = (String) workItem.getParameter("recordObject");
        
        try {
            
            RafRecord recordObject = (RafRecord) rafService.getRafObject(recordObjectId);
            
            //TODO: processId v.s. için ek servis mi çalıştıracağız?
            ProcessInstanceDesc processInstance = runtimeDataService.getProcessInstanceById(workItem.getProcessInstanceId());
            
            
            //FIXME: RootFolder verilmemiş ise ne olacak?
            RafFolder targetFolder = (RafFolder) workItem.getParameter("targetFolder");
                        
            LOG.debug("Process record documents {} exported to {}", recordObject, targetFolder);
            
            rafService.copyObject(recordObject, targetFolder);
            
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
            //FIXME: buarda runtime exception fırlatmak lazım sanırım. İşlem tamamlanamadı sonuçta süreç devam etmemeli!
        }
        
        //Geriye dönecek bir bilgimiz yok!
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //Abort için özel bir işlemimiz yok!
    }
}
