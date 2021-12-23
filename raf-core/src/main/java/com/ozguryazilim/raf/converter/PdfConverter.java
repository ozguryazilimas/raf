package com.ozguryazilim.raf.converter;


import com.google.common.collect.Sets;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;

/**
 *
 * @author oyas
 */
public class PdfConverter {

    private static final PdfConverter INSTANCE = new PdfConverter();
    
    private PdfConverter(){}
    
    private Set<String> acceptedMmimeTypes = Sets.newHashSet("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint",
            "application/msword"
    );
    
    public boolean isAcceptedMimeType(String mimeType){
        return acceptedMmimeTypes.contains(mimeType);
    }
    
    public void convertPreview(InputStream is, String mimeType, OutputStream os) throws OfficeException {

        if (!isAcceptedMimeType(mimeType)) return;
        
        OfficeManagerFactory.getConverter()
                .convert(is).as(DefaultDocumentFormatRegistry.getFormatByMediaType(mimeType))
                .to(os).as(OfficeManagerFactory.toPreviewPdf())
                .execute();


    }

    public void convertReader(InputStream is, String mimeType, OutputStream os) throws OfficeException {

        if (!isAcceptedMimeType(mimeType)) return;

        OfficeManagerFactory.getConverter()
                .convert(is).as(DefaultDocumentFormatRegistry.getFormatByMediaType(mimeType))
                .to(os).as(OfficeManagerFactory.toReaderPdf())
                .execute();
    }

    public static PdfConverter instance(){
        return INSTANCE;
    }
    
}