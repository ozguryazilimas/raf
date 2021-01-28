package com.ozguryazilim.raf.converter;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OfficeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class OfficeManagerFactory {

    private static OfficeManager officeManager;
    private static LocalConverter localConverter;

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
