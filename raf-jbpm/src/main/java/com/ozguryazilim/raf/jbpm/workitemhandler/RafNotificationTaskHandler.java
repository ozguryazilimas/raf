/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.notification.NotificationCommand;
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
public class RafNotificationTaskHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RafNotificationTaskHandler.class);

    @Inject
    private CommandSender commandSender;
    
    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters());
        
        
        NotificationCommand ncm = new NotificationCommand();
        
        ncm.setNotificationClass("ProcessNotification");
        ncm.setSender("SYSTEM");
        
        //FIXME: Buarada TO kısmı virgüllerle ayrılmış gelebilir.
        ncm.setSubject((String) workItem.getParameter("Subject"));
        ncm.setTarget("cs=user;id=" + workItem.getParameter("To"));
        
        Map<String, Object> params = new HashMap<>();
        
        params.put("description", workItem.getParameter("Body"));
        /*
        params.put("TaskDescription", event.getTask().getDescription());
        params.put("TaskId", event.getTask().getId());
        params.put("Task Name", event.getTask().getName());
        params.put("ProcessId", event.getTask().getTaskData().getProcessId());
        params.put("ProcessInstanceId", event.getTask().getTaskData().getProcessInstanceId());
        params.put("DeploymentId", event.getTask().getTaskData().getDeploymentId());
        */
        ncm.setParams(params);
        
        commandSender.sendCommand(ncm);
        
        //Geriye bişi dönmeyeceğiz
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //NŞA'da abort olmamalı!
        LOG.warn("Work Abort!");
    }

}
