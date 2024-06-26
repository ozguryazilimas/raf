package com.ozguryazilim.raf.externaldoc.config;

import com.ozguryazilim.raf.MetadataConfig;
import com.ozguryazilim.raf.MetadataConfigBuilder;
import com.ozguryazilim.raf.MetadataRegistery;
import com.ozguryazilim.raf.externaldoc.search.ExternalSearchPanelController;
import com.ozguryazilim.raf.search.SearchRegistery;
import com.ozguryazilim.telve.api.module.TelveModule;
import org.apache.deltaspike.core.api.config.ConfigResolver;

import javax.annotation.PostConstruct;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafExternalDocModule {

    @PostConstruct
    public void init() {
        MetadataConfig config = MetadataConfigBuilder.of("ExternalDoc")
                .forType("externalDoc:metadata")
                .cnd("externaldoc.cnd")
                .build();

        MetadataRegistery.register(config);

        MetadataConfig configA = MetadataConfigBuilder.of("ExternalDocAnnotation")
                .forType("externalDocAnnotation:metadata")
                .cnd("externalDocAnnotation.cnd")
                .build();

        MetadataRegistery.register(configA);

        MetadataConfig configB = MetadataConfigBuilder.of("ExternalDocMetaTag")
                .forType("externalDocMetaTag:metadata")
                .cnd("externalDocMetaTag.cnd")
                .build();

        MetadataRegistery.register(configB);

        MetadataConfig configC = MetadataConfigBuilder.of("ExternalDocWF")
                .forType("externalDocWF:metadata")
                .cnd("externalDocWF.cnd")
                .build();

        MetadataRegistery.register(configC);

        MetadataConfig configD = MetadataConfigBuilder.of("ExternalDocWFStep")
                .forType("externalDocWFStep:metadata")
                .cnd("externalDocWFStep.cnd")
                .build();

        MetadataRegistery.register(configD);

        boolean isExternalDocSearchPanelActive = ConfigResolver.resolve("raf.searchPanel.active.externaldoc")
                .as(Boolean.class)
                .withDefault(Boolean.FALSE)
                .withCurrentProjectStage(false)
                .getValue();

        if (isExternalDocSearchPanelActive) {
            SearchRegistery.register(ExternalSearchPanelController.class);
        }

    }
}
