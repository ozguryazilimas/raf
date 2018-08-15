/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.converter.config;

import com.ozguryazilim.raf.SequencerRegistery;
import com.ozguryazilim.raf.jod.OfficeManagerFactory;
import com.ozguryazilim.raf.preview.OfficePreviewSequencer;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafConverterModule {
    
    @PostConstruct
    public void init(){
        SequencerRegistery.register("OfficePreviewSequencer", OfficePreviewSequencer.class.getCanonicalName(), 
                "default://*.(odt|odp|ods|xls|doc|ppt|xlsx|docx|pptx)/jcr:content[@jcr:data]");
    }
    
    @PreDestroy
    public void done(){
        //Kapanırken eğer office açıksa kapatalım
        OfficeManagerFactory.stopOfficeManager();
    }
}
