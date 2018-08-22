/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jod;

import java.io.InputStream;
import java.io.OutputStream;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;

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
