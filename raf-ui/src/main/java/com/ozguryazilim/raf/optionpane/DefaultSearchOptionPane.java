package com.ozguryazilim.raf.optionpane;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.DefaultSearchSubPathChangeEvent;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.config.AbstractOptionPane;
import com.ozguryazilim.telve.config.OptionPane;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@OptionPane(permission = "PUBLIC", optionPage = SettingsPages.DefaultSearchOptionPane.class)
public class DefaultSearchOptionPane extends AbstractOptionPane {

    private static final String DEFAULT_SEARCH_PATH_KEY = "default.search.path";

    private static final String ROOT_PATH = "/RAF/";

    private List<SelectItem> rafs;

    private String selectedPath;

    @Inject
    @UserAware
    private Kahve kahve;

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private Identity identity;

    @Inject
    private Event<DefaultSearchSubPathChangeEvent> subPathChangeEvent;

    @PostConstruct
    public void init() {
        List<RafDefinition> rafDefinitions = rafDefinitionService.getRafsForUser(identity.getLoginName());
        if(rafDefinitions != null && !rafDefinitions.isEmpty()){
            rafs = rafDefinitions.stream()
                    .map(raf -> new SelectItem(ROOT_PATH + raf.getCode(), raf.getName()))
                    .collect(Collectors.toList());
        }
        selectedPath = kahve.get(DEFAULT_SEARCH_PATH_KEY, "").getAsString();
    }

    @Override
    @Transactional
    public void save() {
        // Save to kahve user aware configurations
        kahve.put(DEFAULT_SEARCH_PATH_KEY, selectedPath);
        subPathChangeEvent.fire(new DefaultSearchSubPathChangeEvent());
        FacesMessages.info("DefaultSearchOptionPane.message.success");
    }

    public List<SelectItem> getRafs() {
        return rafs;
    }

    public void setRafs(List<SelectItem> rafs) {
        this.rafs = rafs;
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    public void setSelectedPath(String selectedPath) {
        this.selectedPath = selectedPath;
    }
}
