package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.google.common.base.Strings;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    @Inject
    private RafService rafService;
    
    private List<RafDefinition> rafs;
    
    
    private String filter = "";
    private Boolean sortAsc = Boolean.TRUE;
    private Integer size = 10;
    
    @Override
    public void load() {
        super.load(); //To change body of generated methods, choose Tools | Templates.
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName(), Boolean.TRUE);
    }

    public void reGeneratePreviews() throws RafException {
        List<RafObject> rafObjects = rafs.stream().map(RafDefinition::getNodeId).map(nodeId -> {
            try {
                return rafService.getCollection(nodeId).getItems();
            } catch (RafException e) {
                e.printStackTrace();
            }
            return null;
        }).flatMap(Collection::stream).collect(Collectors.toList());
        rafService.reGenerateObjectPreviews(rafObjects);
    }
    
    public List<RafDefinition> getRafs() {
        //TODO: The locale should come from configuration
        Locale locale = new Locale("tr", "TR");

        return rafs.stream()
                //.sorted( sortAsc ? Comparator.comparing(RafDefinition::getName) : Comparator.comparing(RafDefinition::getName))
                .sorted( (r1, r2) -> 
                    sortAsc ? r1.getName().compareTo(r2.getName()) : r1.getName().compareTo(r2.getName()) * -1 
                )
                .filter( r -> Strings.isNullOrEmpty(filter) ? true : r.getName().toLowerCase(locale).contains(filter.toLowerCase(locale)))
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
