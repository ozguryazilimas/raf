/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.invoice.config;

import com.ozguryazilim.raf.MetadataConfig;
import com.ozguryazilim.raf.MetadataConfigBuilder;
import com.ozguryazilim.raf.MetadataConverterRegistery;
import com.ozguryazilim.raf.MetadataRegistery;
import com.ozguryazilim.raf.invoice.InvoiceMetadataConverter;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafInvoiceModule {
 
    @PostConstruct
    public void init(){
        MetadataConfig config = MetadataConfigBuilder.of("Invoice")
                .forType("invoice:metadata")
                .cnd("invoice.cnd")
                .build();
        
        MetadataRegistery.register(config);
        
        MetadataConverterRegistery.register("invoice:metadata", InvoiceMetadataConverter.class);
    }
}
