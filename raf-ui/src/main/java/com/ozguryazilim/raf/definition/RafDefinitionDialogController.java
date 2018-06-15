/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 * Yeni bir Raf tanımlamak için Dialog Controller sınıfı.
 *
 * @author Hakan Uygun
 */
@SessionScoped
@Named
public class RafDefinitionDialogController implements Serializable {

    @Inject
    private RafDefinitionService service;

    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;
    
    private RafDefinition rafDefinition;

    /**
     * Yeni Raf Tanımlama dialoğunu açar.
     *
     */
    public void openDialog() {

        rafDefinition = new RafDefinition();
        
        Map<String, Object> options = new HashMap<>();

        RequestContext.getCurrentInstance().openDialog("/settings/rafDefinitionDialog", options, null);
    }

    public void closeDialog() {
        try {
            service.createNewRaf(rafDefinition);
        } catch (RafException ex) {
            //TODO: i18n
            FacesMessages.error("Raf Tanımlaması Yapılamadı", ex.getMessage());
        }
        
        rafDataChangedEvent.fire(new RafDataChangedEvent());
        
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void cancelDialog() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public RafDefinition getRafDefinition() {
        return rafDefinition;
    }

    public void setRafDefinition(RafDefinition rafDefinition) {
        this.rafDefinition = rafDefinition;
    }

}
