package com.ozguryazilim.raf.converter.config;

import com.ozguryazilim.raf.SequencerRegistery;
import com.ozguryazilim.raf.jod.OfficeManagerFactory;
import com.ozguryazilim.raf.preview.ImagePreviewSequencer;
import com.ozguryazilim.raf.preview.OfficePreviewSequencer;
import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.deltaspike.core.api.config.ConfigResolver;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafConverterModule {
    
    @PostConstruct
    public void init(){
        if( "true".equals(ConfigResolver.getPropertyValue("raf.preview.office", "true"))){
            SequencerRegistery.register("OfficePreviewSequencer", OfficePreviewSequencer.class.getCanonicalName(), 
                "default://*.(odt|odp|ods|xls|doc|ppt|xlsx|docx|pptx)/jcr:content[@jcr:data]");
        }
        SequencerRegistery.register("ImagePreviewSequencer", ImagePreviewSequencer.class.getCanonicalName(), 
                "default://*.(jpg|jpeg|gif|bmp|pcx|png|iff|ras|pbm|pgm|ppm|psd)/jcr:content[@jcr:data]");
    }
    
    @PreDestroy
    public void done(){
        //Kapanırken eğer office açıksa kapatalım
        OfficeManagerFactory.stopOfficeManager();
    }
}
