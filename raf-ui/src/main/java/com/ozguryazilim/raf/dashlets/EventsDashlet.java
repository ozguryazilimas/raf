package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.converters.RafDefinitionConverter;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafEventLog;
import com.ozguryazilim.raf.events.RafEventLogService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import java.util.List;
import java.util.stream.Collectors;
import javax.faces.convert.Converter;
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
    private RafDefinitionService rafDefinitionService;

    @Inject
    private Identity identity;
    
    private List<RafEventLog> events;

    private List<RafDefinition> rafs;

    private RafDefinition selectedRaf;

    public List<RafDefinition> getRafs() {
        return rafs;
    }

    public void setRafs(List<RafDefinition> rafs) {
        this.rafs = rafs;
    }

    public RafDefinition getSelectedRaf() {
        return selectedRaf;
    }

    public void setSelectedRaf(RafDefinition selectedRaf) {
        this.selectedRaf = selectedRaf;
    }

    @Override
    public void load() {
        //Servisten max 10 adet dönüyor.
        events = eventLogService.getEventLogByUser(identity.getLoginName());
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName());
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
        if (selectedRaf == null) {
            events = eventLogService.getEventLogByUser(identity.getLoginName());
        } else {
            events = eventLogService.getEventLogByUserAndRaf(identity.getLoginName(), selectedRaf);
        }
    }

    public Converter getRafDefinitionConverter() {
        return new RafDefinitionConverter(rafs);
    }
}
