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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;

/**
 *
 * @author oyas
 */
@Dashlet(capability = { DashletCapability.canHide, DashletCapability.canEdit, DashletCapability.canRefresh, DashletCapability.canMinimize, DashletCapability.canMaximize })
public class ProcessesDashlet extends AbstractDashlet{
    
    @Inject
    private RuntimeDataService runtimeDataService;
    
    @Inject
    private Identity identity;
    
    private Collection<ProcessInstanceDesc> processes;
    
    @Override
    public void load() {
        List<Integer> states = new ArrayList<>();
        states.add(ProcessInstance.STATE_ACTIVE);
        processes = runtimeDataService.getProcessInstances( states, identity.getLoginName(), new QueryFilter( 0, 10));
    }

    public Collection<ProcessInstanceDesc> getProcesses() {
        return processes;
    }
    
}
