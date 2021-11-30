package com.ozguryazilim.raf.converter;

import java.util.Arrays;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFamily;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.AbstractConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.office.LocalOfficeManager.Builder;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;

/**
 *
 * @author oyas
 */
public class OfficeManagerFactory {

    private static OfficeManager officeManager;
    private static AbstractConverter converter;

    private static Map<String, Object> customProperties;
    private static DocumentFormat previewPdf;
    
    private static final String FILTER_DATA_PROP = "FilterData";
    private static final String LOCAL_OFFICE = "local";
    private static final String REMOTE_OFFICE = "remote";
    
    public static final String PROP_OFFICE_TYPE = "raf.office.type";
    public static final String PROP_OFFICE_PORT = "raf.office.port";
    public static final String PROP_OFFICE_HOME = "raf.office.home";
    public static final String PROP_OFFICE_URL  = "raf.office.url";
    public static final String PROP_OFFICE_TEMPLATE  = "raf.office.template";
    
    private OfficeManagerFactory() {
    }

    /**
     * Geriye Kullanılacak OfficeManager'ı döndürür.
     *
     * @return
     */
    public synchronized static OfficeManager getOfficeManager() throws OfficeException {
        if (officeManager == null) {
            String officeType = ConfigResolver.getPropertyValue( PROP_OFFICE_TYPE, LOCAL_OFFICE);
            if ( LOCAL_OFFICE.equals(officeType)){
                Builder builder = LocalOfficeManager.builder();
                
                if (ConfigResolver.getPropertyValue( PROP_OFFICE_HOME ) != null) {
                    builder.hostName(ConfigResolver.getPropertyValue( PROP_OFFICE_HOME ));
                }
                
                if (ConfigResolver.getPropertyValue( PROP_OFFICE_TEMPLATE ) != null) {
                    builder.templateProfileDirOrDefault(ConfigResolver.getPropertyValue( PROP_OFFICE_TEMPLATE ));
                }
                
                if (ConfigResolver.getPropertyValue( PROP_OFFICE_URL ) != null) {
                    builder.hostName(ConfigResolver.getPropertyValue( PROP_OFFICE_URL ));
                }
                
                if (ConfigResolver.getPropertyValue( PROP_OFFICE_PORT ) != null) {
                    String p = ConfigResolver.getPropertyValue( PROP_OFFICE_PORT );
                    
                    int[] ports = Arrays.stream(p.split(","))
                                                .map(String::trim)
                                                .mapToInt(Integer::valueOf)
                                                .toArray();
                    builder.portNumbers(ports);
                }
                        
                officeManager = builder.build();
            } else {
                String officeUrl = ConfigResolver.getPropertyValue(PROP_OFFICE_URL);
                officeManager = RemoteOfficeManager.install(officeUrl);
            }
            officeManager.start();
        }

        return officeManager;
    }
    
    
    public static Map<String, Object> getCustomProperties(){
        if( customProperties == null ){
            String range = ConfigResolver.getPropertyValue("raf.preview.office.range", "1-2");

            Map<String, Object> filterData = new HashMap<>();
            //Eğer ALL gelirise hepsi çevrilecek demek
            if( !"ALL".equals(range)){
                filterData.put("PageRange", range);
            }
            customProperties = new HashMap<>();
            customProperties.put(FILTER_DATA_PROP, filterData);
        }
        
        return customProperties;

    }
    
    public static Map<String, Object> getFilterData(){
        return (Map<String, Object>) getCustomProperties().get(FILTER_DATA_PROP);
    }

    public static DocumentFormat toPreviewPdf(){
        
        if( previewPdf == null ){
            previewPdf = DocumentFormat.builder().from(DefaultDocumentFormatRegistry.PDF)
                .storeProperty(DocumentFamily.TEXT, FILTER_DATA_PROP, getFilterData())
                .storeProperty(DocumentFamily.SPREADSHEET, FILTER_DATA_PROP, getFilterData())
                .storeProperty(DocumentFamily.PRESENTATION, FILTER_DATA_PROP, getFilterData())
                .storeProperty(DocumentFamily.DRAWING, FILTER_DATA_PROP, getFilterData())
                .build();
        }
        
        
        return previewPdf;
    }
    
    public static AbstractConverter getConverter() throws OfficeException {
        if( converter == null ){
            String officeType = ConfigResolver.getPropertyValue( PROP_OFFICE_TYPE, LOCAL_OFFICE);
            
            
            
            if( LOCAL_OFFICE.equals(officeType)){
                
                
                
                converter = LocalConverter.builder()
                        .officeManager(getOfficeManager())
                        //.storeProperties(getCustomProperties())
                        .build();
            } else {
                //Filter data office manager üzerinden alınmıyor
                //LibreOffice Online FilterData bilgisini işlemiyor
                converter = RemoteConverter.builder()
                        .officeManager(getOfficeManager())
                        .build();
            }
        }

        return converter;
    }

    public static void stopOfficeManager(){
        OfficeUtils.stopQuietly(officeManager);
    }

}
