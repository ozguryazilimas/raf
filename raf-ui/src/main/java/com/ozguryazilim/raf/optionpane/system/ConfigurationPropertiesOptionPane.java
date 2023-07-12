package com.ozguryazilim.raf.optionpane.system;

import com.ozguryazilim.raf.ConfigurationPropertiesService;
import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.raf.model.RafConfigurationProperty;
import com.ozguryazilim.telve.config.AbstractOptionPane;
import com.ozguryazilim.telve.config.OptionPane;
import com.ozguryazilim.telve.config.OptionPaneType;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@OptionPane(optionPage = SettingsPages.ConfigurationPropertiesOptionPane.class, type = OptionPaneType.System)
public class ConfigurationPropertiesOptionPane extends AbstractOptionPane {

    @Inject
    private ConfigurationPropertiesService configurationPropertiesService;

    public List<RafConfigurationProperty> getConfigurationProperties() {
        return configurationPropertiesService.getConfigurationPropertiesMap().entrySet().stream()
                .map((entry) -> new RafConfigurationProperty(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
