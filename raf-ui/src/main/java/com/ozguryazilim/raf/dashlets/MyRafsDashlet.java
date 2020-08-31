package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
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
import java.util.List;
import java.util.stream.Collectors;
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

    @Inject
    private RafService rafService;
    
    private List<RafDefinition> rafs;
    
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
