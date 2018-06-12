/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.pdf.config;

import com.ozguryazilim.raf.MetadataConverterRegistery;
import com.ozguryazilim.raf.SequencerRegistery;
import com.ozguryazilim.raf.pdf.PdfMetadataConverter;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafPdfModule {
    
    @PostConstruct
    public void init(){
        MetadataConverterRegistery.register("pdf:metadata", PdfMetadataConverter.class);
        
        SequencerRegistery.register("PDFSequencer", "pdf", "default://*.(pdf)/jcr:content[@jcr:data]");
        
    }
}
