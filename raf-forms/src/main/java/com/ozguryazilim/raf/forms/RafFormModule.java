/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms;

import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafFormModule {
    
    @Inject
    private FormManager formManager;
    
    @PostConstruct
    public void init(){
        formManager.getForm("");
    }
            
}
