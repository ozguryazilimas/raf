package com.ozguryazilim.raf;

import org.apache.deltaspike.core.api.config.ConfigResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.Map;

@ApplicationScoped
@Named
public class ConfigurationPropertiesService {

    public Map<String, String> getConfigurationPropertiesMap() {
        return ConfigResolver.getAllProperties();
    }
}
