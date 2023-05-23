package com.ozguryazilim.raf.optionpane.system;

import com.ozguryazilim.raf.ReadOnlyModeService;
import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.telve.config.AbstractOptionPane;
import com.ozguryazilim.telve.config.OptionPane;
import com.ozguryazilim.telve.config.OptionPaneType;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@OptionPane(optionPage = SettingsPages.ReadOnlyModeOptionPane.class, type = OptionPaneType.System)
public class ReadOnlyModeOptionPane extends AbstractOptionPane {

    @Inject
    private ReadOnlyModeService readOnlyModeService;

    private String rafReadOnlyCurrentState = ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY.getDefaultValue();

    @PostConstruct
    public void init() {
        rafReadOnlyCurrentState = readOnlyModeService.getState(ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY);
    }

    @Override
    @Transactional
    public void save() {
        readOnlyModeService.setState(ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY, rafReadOnlyCurrentState);

        FacesMessages.info("ReadOnlyModeOptionPane.message.success", "");
    }

    public List<String> getRafReadOnlyStatePosibleValues() {
        return Arrays.asList(ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY.getValues());
    }

    public ReadOnlyModeService.ReadOnlyState getRafReadOnlyState() {
        return ReadOnlyModeService.ReadOnlyState.RAF_READ_ONLY;
    }

    public String getRafReadOnlyCurrentState() {
        return rafReadOnlyCurrentState;
    }

    public void setRafReadOnlyCurrentState(String rafReadOnlyCurrentState) {
        this.rafReadOnlyCurrentState = rafReadOnlyCurrentState;
    }
}
