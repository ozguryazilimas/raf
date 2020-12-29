package com.ozguryazilim.raf.dashlets;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@Dashlet(capability = { DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canEdit }, permission = "public")
public class MyRafsDashlet extends AbstractDashlet{

    @Inject
    private RafDefinitionService rafDefinitionService;
    
    @Inject
    private Identity identity;
    
    private List<RafDefinition> rafs;
    
    
    private String filter = "";
    private Boolean sortAsc = Boolean.TRUE;
    private Integer size = 10;
    
    @Override
    public void load() {
        super.load(); //To change body of generated methods, choose Tools | Templates.
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName(), Boolean.TRUE);
    }
    
    public List<RafDefinition> getRafs() {
        return rafs.stream()
                //.sorted( sortAsc ? Comparator.comparing(RafDefinition::getName) : Comparator.comparing(RafDefinition::getName))
                .sorted( (r1, r2) -> 
                    sortAsc ? r1.getName().compareTo(r2.getName()) : r1.getName().compareTo(r2.getName()) * -1 
                )
                .filter( r -> Strings.isNullOrEmpty(filter) ? true : r.getName().contains(filter))
                .limit(size)
                .collect(Collectors.toList());
    }
    
    /**
     * Yeni raf eklenir,silinir ya da ismi değişirse dashlet'i güncelle
     * @param event 
     */
    public void rafDataChangedListener(@Observes RafDataChangedEvent event) {
        load();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Boolean getSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(Boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
    
    
    
}
