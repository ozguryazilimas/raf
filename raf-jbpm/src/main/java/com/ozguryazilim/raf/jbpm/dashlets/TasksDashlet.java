/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.dashlets;

import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import java.util.List;
import javax.inject.Inject;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;

/**
 *
 * @author oyas
 */
@Dashlet(capability = { DashletCapability.canHide, DashletCapability.canEdit, DashletCapability.canRefresh, DashletCapability.canMinimize, DashletCapability.canMaximize })
public class TasksDashlet extends AbstractDashlet{
    
    @Inject
    private RuntimeDataService runtimeDataService;
    
    @Inject
    private Identity identity;
    
    private List<TaskSummary> tasks;
    
    @Override
    public void load() {
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(identity.getLoginName(), null);
    }

    public List<TaskSummary> getTasks() {
        return tasks;
    }
    
}
