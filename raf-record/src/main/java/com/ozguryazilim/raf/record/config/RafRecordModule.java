package com.ozguryazilim.raf.record.config;

import com.ozguryazilim.raf.record.search.RecordSearchPanelController;
import com.ozguryazilim.raf.search.SearchRegistery;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafRecordModule {

    @PostConstruct
    public void init() {
        SearchRegistery.register(RecordSearchPanelController.class);

    }
}
