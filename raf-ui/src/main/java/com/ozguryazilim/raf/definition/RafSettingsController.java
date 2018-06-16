/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.definition;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.config.RafPages;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class RafSettingsController implements Serializable{

    private static final Logger LOG = LoggerFactory.getLogger(RafSettingsController.class);
    
    @Inject
    private RafDefinitionService definitionService;
    
    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;
    
    private RafDefinition rafDefinition;
    private String rafCode;
    
    public void init(){
        if (Strings.isNullOrEmpty(rafCode)) {
            rafCode = "PRIVATE";
        }

        try {
            rafDefinition = definitionService.getRafDefinitionByCode(rafCode);
        } catch (RafException ex) {
            //FIXME: Burada ne yapmalı?
            LOG.error("Error", ex);
        }

    }

    public String getRafCode() {
        return rafCode;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public RafDefinition getRafDefinition() {
        return rafDefinition;
    }

    public void setRafDefinition(RafDefinition rafDefinition) {
        this.rafDefinition = rafDefinition;
    }
    
    public Class<? extends ViewConfig> save(){
        
        try {
            definitionService.save( rafDefinition);
            rafDataChangedEvent.fire(new RafDataChangedEvent());
        
            return RafPages.class;
        } catch (RafException ex) {
            LOG.error("Raf Update error", ex);
            FacesMessages.error("Raf Update error", ex.getLocalizedMessage());
        }
        
        return null;
    }
    
    public Class<? extends ViewConfig> cancel(){
        
        return RafPages.class;
    }
}
