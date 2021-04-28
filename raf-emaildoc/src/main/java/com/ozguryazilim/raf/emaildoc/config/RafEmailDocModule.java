package com.ozguryazilim.raf.emaildoc.config;

import com.ozguryazilim.raf.MetadataConfig;
import com.ozguryazilim.raf.MetadataConfigBuilder;
import com.ozguryazilim.raf.MetadataRegistery;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafEmailDocModule {

    @PostConstruct
    public void init() {
        MetadataConfig config = MetadataConfigBuilder.of("emaildoc")
                .forType("emaildoc:metadata")
                .cnd("emaildoc.cnd")
                .build();

        MetadataRegistery.register(config);
    }
}
