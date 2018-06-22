/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
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
    
    private static final List<String> knowMimeTypes = Arrays.asList(
            "image-png", "image-jpeg", "image-bmp", "image-svg-xml", "image-gif", 
            "application-pdf", "text-plain", "text-x-asciidoc", "text-x-web-markdown",
            "text-csv", "application-x-sh", "text-html", "application-xml", "text-x-log",
            "application-x-mobipocket-ebook", "application-epub-zip", 
            "video-webm", "video-x-matroska", "video-x-mp4",
            "audio-mp3", "audio-mpeg", 
            "application-zip", "application-gzip", "application-x-bittorrent",
            "application-vnd-openxmlformats-officedocument-spreadsheetml-sheet",
            "application-vnd-openxmlformats-officedocument-presentationml-presentation",
            "application-vnd-openxmlformats-officedocument-wordprocessingml-document",
            "application-vnd-oasis-opendocument-spreadsheet",
            "application-vnd-oasis-opendocument-presentation",
            "application-vnd-oasis-opendocument-text",
            "application-vnd-ms-excel",
            "application-vnd-ms-powerpoint",
            "application-msword"
            );
    
    public String getIcon( String mimeType ){
        LOG.debug("Icon ask for MimeType : {}", mimeType);
        switch (mimeType) {
            case "raf/folder":
                return "folder";
            case "raf/categories":
                return "fa-sitemap";
            case "raf/tags":
                return "fa-tags";
            case "raf/search":
                return "fa-search";

            default:
                String s = mimeType.replace("/", "-").replace(".", "-").replace("+", "-");
                if( knowMimeTypes.contains(s)){
                    return s;
                } else {
                    //FIXME: burda aslında ana gruplara bakılabilir text-*, video* v.b. en son olarak da Unknown
                    return "text-plain";
                }
                
                
                
                /*
                
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
            */
        }
    }
    
}
