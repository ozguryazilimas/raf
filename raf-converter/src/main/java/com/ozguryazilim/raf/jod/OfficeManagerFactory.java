/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jod;

import java.util.HashMap;
import java.util.Map;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OfficeUtils;

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
    public synchronized static OfficeManager getOfficeManager() throws OfficeException{
        //FIXME: burada office ayarlarının alınması gerekecek
        if( officeManager == null ){
            officeManager = LocalOfficeManager.install();
            officeManager.start();
        }
        
        return officeManager;
    }

    public static LocalConverter getLocalConverter() throws OfficeException {
        if( localConverter == null ){
            //FIXME: burada ayarlardan alınacak bilgiler
            Map<String, Object> filterData = new HashMap<>();
            filterData.put("PageRange", "1-2");
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
