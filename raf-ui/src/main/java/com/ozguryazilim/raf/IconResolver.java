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
            "application-x-mobipocket-ebook", "application-epub-zip", "application-bpmn-xml", 
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
        LOG.trace("Icon ask for MimeType : {}", mimeType);
        switch (mimeType) {
            case "raf/folder":
                return "folder";
            case "raf/record":
                return "record";
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
                    LOG.debug("Icon ask for MimeType and not found: {}", mimeType);
                    return "text-plain";
                }
        }
    }
    
}
