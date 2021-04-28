package com.ozguryazilim.raf.ocr.config;

import com.ozguryazilim.telve.api.module.TelveModule;
import javax.annotation.PostConstruct;
import net.sourceforge.tess4j.Tesseract;
import org.apache.deltaspike.core.api.config.ConfigResolver;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafOcrModule {

    @PostConstruct
    public void init() {
        Boolean ocrEnabled = Boolean.parseBoolean(ConfigResolver.getPropertyValue("raf.ocr.enabled", "false"));
        if (ocrEnabled) {
            RafOcrConfig.tessDataPath = ConfigResolver.getPropertyValue("raf.ocr.tessdatapath", "/usr/share/tesseract-ocr/4.00/tessdata");
            RafOcrConfig.language = ConfigResolver.getPropertyValue("raf.ocr.language", "tur");
            RafOcrConfig.tesseract = new Tesseract();
            RafOcrConfig.tesseract.setDatapath(RafOcrConfig.tessDataPath);
            RafOcrConfig.tesseract.setLanguage(RafOcrConfig.language);
        }
    }
}
