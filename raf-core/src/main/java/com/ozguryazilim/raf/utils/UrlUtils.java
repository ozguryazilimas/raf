package com.ozguryazilim.raf.utils;

import org.apache.deltaspike.core.api.config.ConfigResolver;

public class UrlUtils {

    private UrlUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getDocumentShareURL(String code) {
        return ConfigResolver.getPropertyValue("app.linkDomain") + "public/download.jsf?code=" + code;
    }

}