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

/**
 * Sistem olaylarını sunar.
 * 
 * FIXME: Veri servisi implement edildiğinde düzeltilecek.
 * 
 * @author Hakan Uygun
 */
@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canRefresh}, permission = "public")
public class EventsDashlet extends AbstractDashlet{
   
    @Inject
    private RafEventLogService eventLogService;
    
    @Inject
    private Identity identity;
    
    private List<RafEventLog> events;

    @Override
    public void load() {
        Map<String, String> toBeReplacedWords = new HashMap<>();
        toBeReplacedWords.putAll(getRolesLocalizedMessages());

        //Servisten max 10 adet dönüyor.
        events = eventLogService.getEventLogByUser(identity.getLoginName());
        events.forEach(rafEventLog -> rafEventLog.setMessage(clearHtmlTags(rafEventLog.getMessage())));
        events.forEach(message ->
                message.setMessage(StringUtils.replaceEach(message.getMessage(), toBeReplacedWords.keySet().toArray(new String[0]), toBeReplacedWords.values().toArray(new String[0]))));
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

    private Map<String, String> getRolesLocalizedMessages() {
        Map<String, String> userRoleMessages = new HashMap<>();
        Arrays.stream(RafMemberRole.values()).forEach(role -> userRoleMessages.put(role.name(), Messages.getMessage("member.role." + role)));
        return userRoleMessages;
    }
}
