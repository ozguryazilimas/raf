package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.KahveKey;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.events.RafEventLogService;
import com.ozguryazilim.raf.objet.member.RafMemberRole;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import com.ozguryazilim.telve.messages.Messages;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sistem olaylarını sunar.
 * 
 * FIXME: Veri servisi implement edildiğinde düzeltilecek.
 * 
 * @author Hakan Uygun
 */
@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canRefresh, DashletCapability.canEdit}, permission = "public")
public class EventsDashlet extends AbstractDashlet{
   
    @Inject
    private RafEventLogService eventLogService;
    
    @Inject
    private Identity identity;

    @Inject
    private RafDefinitionService rafDefinitionService;

    @UserAware
    @Inject
    private Kahve kahve;

    private List<RafEventLog> events;

    private String rafFilter;
    private String eventFilter;

    @PostConstruct
    @Override
    public void init() {
        rafFilter = kahve.get(EventsDashletFilterKeys.RAF_FILTER).getAsString();
        eventFilter = kahve.get(EventsDashletFilterKeys.EVENT_FILTER).getAsString();

        //Check if user still belongs to raf
        if (!"*".equals(rafFilter)) {
            boolean isUserRegisteredToRaf = rafDefinitionService.getRafsForUser(identity.getLoginName()).stream()
                    .anyMatch(rafDefinition -> rafFilter.equals(rafDefinition.getName()));

            if (isUserRegisteredToRaf) {
                kahve.put(EventsDashletFilterKeys.RAF_FILTER, EventsDashletFilterKeys.RAF_FILTER.defaultValue);
                rafFilter = EventsDashletFilterKeys.RAF_FILTER.defaultValue;
            }
        }

        load();
    }

    @Override
    public void load() {
        Map<String, String> toBeReplacedWords = new HashMap<>();
        toBeReplacedWords.putAll(getRolesLocalizedMessages());

        //Servisten max 10 adet dönüyor.
        events = eventLogService.getEventLogByUser(identity.getLoginName(), rafFilter, eventFilter);
        events.forEach((RafEventLog rafEventLog) -> {
            rafEventLog.setMessage(clearHtmlTags(rafEventLog.getMessage()));
            rafEventLog.setMessage(StringUtils.replaceEach(rafEventLog.getMessage(), toBeReplacedWords.keySet().toArray(new String[0]), toBeReplacedWords.values().toArray(new String[0])));
        });
    }

    public List<RafEventLog> getEvents() {
        return events;
    }

    @Override
    public void refresh() {
        load();
    }

    @Override
    public void save() {
        kahve.put(EventsDashletFilterKeys.RAF_FILTER, eventFilter);
        kahve.put(EventsDashletFilterKeys.EVENT_FILTER, rafFilter);

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

    public List<String> getRafs() {
        return rafDefinitionService.getRafsForUser(identity.getLoginName(), false).stream()
                .map(RafDefinition::getName)
                .collect(Collectors.toList());
    }

    public List<String> getEventTypes() {
        return eventLogService.getEventLogTypes();
    }

    public String getRafFilter() {
        return rafFilter;
    }

    public void setRafFilter(String rafFilter) {
        this.rafFilter = rafFilter;
    }

    public String getEventFilter() {
        return eventFilter;
    }

    public void setEventFilter(String eventFilter) {
        this.eventFilter = eventFilter;
    }

    enum EventsDashletFilterKeys implements KahveKey {
        RAF_FILTER("eventsdashlet.property.raf_filter", "*"),
        EVENT_FILTER("eventsdashlet.property.raf_filter", "*");

        private String key;
        private String defaultValue;

        EventsDashletFilterKeys(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultValue() {
            return this.defaultValue;
        }
    }
}
