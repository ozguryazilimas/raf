package com.ozguryazilim.raf;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author oyas
 */
public class MetadataConverterRegistery {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataConverterRegistery.class);
    
    private static Map<String,Class<? extends MetadataConverter>> converters = new HashMap<>();
    private static Map<String,MetadataConverter> instances = new HashMap<>();
    
    private static MetadataConverter defaultConverter = new DefaultMetadataConverter();
    
    public static void register( String type, Class<? extends MetadataConverter> converterClass ){
        converters.put(type, converterClass);
        LOG.info("MetadataConverter {} registered for : {}", converterClass.getSimpleName(), type);
    }
    
    /**
     * Verilen tipe uygun converter varsa onu yoksa DefaultMetadaraConverter'ı döndürür.
     * 
     * 
     * @param type
     * @return 
     */
    public static MetadataConverter getConverter( String type ){
        
        //Burada performance için intance cahce tutuluyor.
        
        MetadataConverter result = instances.get(type);
        
        if( result == null ){
        
            Class<? extends MetadataConverter> clazz = converters.get(type);
            if( clazz == null ){
                return getDefaultMetadataConverter();
            } else {
                try {
                    result = clazz.newInstance();
                    instances.put(type, result);
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOG.error("Cannot create converter class : {}", clazz, ex);
                    return getDefaultMetadataConverter();
                }
            }
        }
        
        return result;
    }
    
    public static MetadataConverter getDefaultMetadataConverter(){
        return defaultConverter;
    }
}
