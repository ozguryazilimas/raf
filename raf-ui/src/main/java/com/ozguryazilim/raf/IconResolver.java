/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MimeType'a göre geriye icon ismi çevirir.
 * 
 * FIXME: mimeType - icon mapplingi bir conf dosyasından bir yerden alınmalı.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
@Named
public class IconResolver implements Serializable{
   
    private static final Logger LOG = LoggerFactory.getLogger(IconResolver.class);
    
    public String getIcon( String mimeType ){
        LOG.debug("Icon ask for MimeType : {}", mimeType);
        switch (mimeType) {
            case "raf/folder":
                return "fa-folder";
            case "raf/categories":
                return "fa-sitemap";
            case "raf/tags":
                return "fa-tags";
            case "raf/search":
                return "fa-search";
                
            case "application/pdf":
                return "fa-file-pdf-o";
            
            case "image/png":
            case "image/bmp":
            case "image/jpg":
            case "image/jpeg":
                return "fa-file-image-o";
            
            case "text/plain":
            case "text/x-web-markdown":
            case "text/x-asciidoc":
                return "fa-file-text-o";
                
            case "application/xml":
            case "application/x-sh":
            case "text/html":
                return "fa-file-code-o";
                
                
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            case "application/vnd.oasis.opendocument.text":
                return "fa-file-word-o";
                
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            case "application/vnd.ms-excel":
            case "application/vnd.oasis.opendocument.spreadsheet":
                return "fa-file-excel-o";
            
            case "application/vnd.ms-powerpoint":
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
            case "application/vnd.oasis.opendocument.presentation":
                return "fa-file-powerpoint-o";
                
            default:
                return "fa-file-o";
        }
    }
    
}
