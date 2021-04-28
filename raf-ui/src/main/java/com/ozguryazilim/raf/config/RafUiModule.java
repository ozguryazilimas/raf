package com.ozguryazilim.raf.config;

import com.ozguryazilim.raf.search.GenericSearchPanelController;
import com.ozguryazilim.raf.search.SearchRegistery;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author oyas
 */
@TelveModule
public class RafUiModule {

    @PostConstruct
    public void init() {
        SearchRegistery.register(GenericSearchPanelController.class);
    }
}
