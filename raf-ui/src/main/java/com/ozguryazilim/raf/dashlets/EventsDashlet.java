/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.events.RafEventLogService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import java.util.List;
import javax.inject.Inject;

/**
 * Sistem olaylarını sunar.
 * 
 * FIXME: Veri servisi implement edildiğinde düzeltilecek.
 * 
 * @author Hakan Uygun
 */
@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canEdit, DashletCapability.canMinimize, DashletCapability.canRefresh})
public class EventsDashlet extends AbstractDashlet{
   
    @Inject
    private RafEventLogService eventLogService;
    
    @Inject
    private Identity identity;
    
    private List<RafEventLog> events;

    @Override
    public void load() {
        //FIXME: Aslında eventLogService'den limitli almak gerekiyor.
        events = eventLogService.getEventLogByUser(identity.getLoginName());
        if( events.size() > 10 ){
            events = events.subList(0, 10);
        }
    }

    public List<RafEventLog> getEvents() {
        return events;
    }

    @Override
    public void refresh() {
        load();
    }
    
    
    
}
