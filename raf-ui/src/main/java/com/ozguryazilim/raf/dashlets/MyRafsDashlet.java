package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import com.ozguryazilim.telve.nav.NagivationController;
import java.util.List;
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
    
}
