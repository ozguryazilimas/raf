package com.ozguryazilim.raf.converter;


import java.io.InputStream;
import java.io.OutputStream;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;

/**
 *
 * @author oyas
 */
public class PDFPreviewConverter {

    public void convert(InputStream is, String mimeType, OutputStream os) throws OfficeException {

        OfficeManagerFactory.getLocalConverter()
                .convert(is).as(DefaultDocumentFormatRegistry.getFormatByMediaType(mimeType))
                .to(os).as(DefaultDocumentFormatRegistry.PDF)
                .execute();


    }

}