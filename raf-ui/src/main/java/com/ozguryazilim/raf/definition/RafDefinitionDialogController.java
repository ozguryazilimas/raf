package com.ozguryazilim.raf.definition;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.action.CreateFolderAction;
import com.ozguryazilim.raf.config.RafPages;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
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
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Yeni bir Raf tanımlamak için Dialog Controller sınıfı.
 *
 * @author Hakan Uygun
 */
@SessionScoped
@Named
public class RafDefinitionDialogController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafDefinitionDialogController.class);

    @Inject
    private RafDefinitionService service;

    @Inject
    private RafService rafService;

    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    private RafDefinition rafDefinition;

    private boolean isCreated;

    /**
     * Yeni Raf Tanımlama dialoğunu açar.
     *
     */
    public void openDialog() {

        rafDefinition = new RafDefinition();

        isCreated = false;

        Map<String, Object> options = new HashMap<>();

        RequestContext.getCurrentInstance().openDialog("/settings/rafDefinitionDialog", options, null);
    }

    public void closeDialog() {
        try {
            if (rafService.checkRafName(rafDefinition.getName())) {

                service.createNewRaf(rafDefinition);
                rafDataChangedEvent.fire(new RafDataChangedEvent());
                isCreated = true;
                //Burada nasıl davranıyor ki?
                RequestContext.getCurrentInstance().closeDialog(null);
            }
        } catch (RafException ex) {
            //TODO: i18n
            FacesMessages.error("Raf Tanımlaması Yapılamadı", ex.getMessage());
            LOG.error("Raf Tanımlaması Yapılamadı", ex);
        }

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

    public void goCreatedRaf() {
        if(isCreated){
            navigationParameterContext.addPageParameter("id", rafDefinition.getCode());
            viewNavigationHandler.navigateTo(RafPages.class);
        }
    }
    
    public void onNameChange(){
        if (rafService.checkRafName(rafDefinition.getName())) {
            RafEncoder encoder = RafEncoderFactory.getRafNameEncoder();
            //TODO aslında code içinde bir şey var ise bunu yapmasak mı?
            rafDefinition.setCode(encoder.encode(rafDefinition.getName()));
        }
    }
}
