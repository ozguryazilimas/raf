package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.events.RafEventLogService;
import com.ozguryazilim.raf.objet.member.RafMemberRole;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import com.ozguryazilim.telve.messages.Messages;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canRefresh}, permission = "public")
public class RecentlyDashlet extends AbstractDashlet{
   
    @Inject
    private RafEventLogService eventLogService;
    
    @Inject
    private Identity identity;
    
    private List<RafEventLog> events;

    @Override
    public void load() {
        //Servisten max 10 adet dönüyor.
        events = eventLogService.getRecentlyEventsByUser(identity.getLoginName());
        events.forEach((RafEventLog rafEventLog) -> rafEventLog.setMessage(clearHtmlTags(rafEventLog.getMessage())));
    }

    public List<RafEventLog> getEvents() {
        return events;
    }

    @Override
    public void refresh() {
        load();
    }

    private String clearHtmlTags(String input) {
        return input.replaceAll("<", "&lt").replaceAll(">", "&gt");
    }

}
