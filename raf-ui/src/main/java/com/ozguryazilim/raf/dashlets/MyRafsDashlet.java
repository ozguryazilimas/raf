package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import java.util.List;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@Dashlet(capability = { DashletCapability.canHide, DashletCapability.canMinimize })
public class MyRafsDashlet extends AbstractDashlet{

    @Inject
    private RafDefinitionService rafDefinitionService;
    
    @Inject
    private Identity identity;
    
    private List<RafDefinition> rafs;
    
    @Override
    public void load() {
        super.load(); //To change body of generated methods, choose Tools | Templates.
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName(), Boolean.TRUE);
    }
    
    public List<RafDefinition> getRafs() {
        return rafs;
    }
    
    /**
     * Yeni raf eklenir,silinir ya da ismi değişirse dashlet'i güncelle
     * @param event 
     */
    public void rafDataChangedListener(@Observes RafDataChangedEvent event) {
        load();
    }
    
}
