package com.ozguryazilim.raf.converter;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
//import org.jodconverter.local.office.OfficeException;
//import org.jodconverter.local.office.OfficeManager;
//import org.jodconverter.local.office.OfficeUtils;

import java.util.HashMap;
import java.util.Map;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;

/**
 *
 * @author oyas
 */
public class OfficeManagerFactory {

    private static OfficeManager officeManager;
    private static LocalConverter localConverter;

    private OfficeManagerFactory() {
    }

    /**
     * Geriye Kullanılacak OfficeManager'ı döndürür.
     *
     * @return
     */
    public synchronized static OfficeManager getOfficeManager() throws OfficeException {
        //FIXME: burada office ayarlarının alınması gerekecek
        if( officeManager == null ){
            officeManager = LocalOfficeManager.install();
            officeManager.start();
        }

        return officeManager;
    }

    public static LocalConverter getLocalConverter() throws OfficeException {
        if( localConverter == null ){
            String range = ConfigResolver.getPropertyValue("raf.preview.office.range", "1-2");

            Map<String, Object> filterData = new HashMap<>();
            //Eğer ALL gelirise hepsi çevrilecek demek
            if( !"ALL".equals(range)){
                filterData.put("PageRange", range);
            }
            Map<String, Object> customProperties = new HashMap<>();
            customProperties.put("FilterData", filterData);

            localConverter = LocalConverter.builder()
                    .officeManager(getOfficeManager())
                    .storeProperties(customProperties)
                    .build();
        }

        return localConverter;
    }

    public static void stopOfficeManager(){
        OfficeUtils.stopQuietly(officeManager);
    }

}
