package com.ozguryazilim.raf.definition;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.config.RafPages;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.view.Pages;
import java.io.Serializable;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class RafSettingsController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(RafSettingsController.class);

    @Inject
    private Identity identity;

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafDefinitionService definitionService;

    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;

    private RafDefinition rafDefinition;
    private String rafCode;

    public void init() {
        if (Strings.isNullOrEmpty(rafCode)) {
            rafCode = "PRIVATE";
        }

        try {
            rafDefinition = definitionService.getRafDefinitionByCode(rafCode);
        } catch (RafException ex) {
            //FIXME: Burada ne yapmalı?
            LOG.error("Error", ex);
            viewNavigationHandler.navigateTo(Pages.Home.class);
        }

        try {
            //Uye değilse hemen HomePage'e geri gönderelim.
            if (!memberService.isMemberOf(identity.getLoginName(), rafDefinition)) {
                viewNavigationHandler.navigateTo(Pages.Home.class);
            } else if (!memberService.hasManagerRole(identity.getLoginName(), rafDefinition)) {
                navigationParameterContext.addPageParameter("id", rafDefinition.getCode());
                viewNavigationHandler.navigateTo(RafPages.class);
            }
        } catch (RafException ex) {
            LOG.error("Error", ex);
            //Gene de geldiği yere gönderelim.
            viewNavigationHandler.navigateTo(Pages.Home.class);
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

    public Class<? extends ViewConfig> save() {

        try {
            definitionService.save(rafDefinition);
            rafDataChangedEvent.fire(new RafDataChangedEvent());

            return RafPages.class;
        } catch (RafException ex) {
            LOG.error("Raf Update error", ex);
            FacesMessages.error("Raf Update error", ex.getLocalizedMessage());
        }

        return null;
    }

    public Class<? extends ViewConfig> cancel() {

        return RafPages.class;
    }
}