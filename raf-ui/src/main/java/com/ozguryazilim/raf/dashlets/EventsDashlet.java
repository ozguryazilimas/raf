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
@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canEdit, DashletCapability.canMinimize, DashletCapability.canRefresh}, permission = "public")
public class EventsDashlet extends AbstractDashlet{
   
    @Inject
    private RafEventLogService eventLogService;
    
    @Inject
    private Identity identity;
    
    private List<RafEventLog> events;

    @Override
    public void load() {
        //Servisten max 10 adet dönüyor.
        events = eventLogService.getEventLogByUser(identity.getLoginName());
    }

    public List<RafEventLog> getEvents() {
        return events;
    }

    @Override
    public void refresh() {
        load();
    }
    
    
    
}
