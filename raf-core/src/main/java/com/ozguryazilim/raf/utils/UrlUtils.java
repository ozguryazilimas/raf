package com.ozguryazilim.raf.utils;

import org.apache.deltaspike.core.api.config.ConfigResolver;

public class UrlUtils {

    private UrlUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getDocumentShareURL(String code) {
        StringBuilder sb = new StringBuilder();
        String linkDomain = ConfigResolver.getPropertyValue("app.linkDomain");
        String rafShareUrlSuffix = "public/download.jsf?code=" + code;

        sb.append(linkDomain)
            .append(linkDomain.endsWith("/") ? "" : '/')
            .append(rafShareUrlSuffix);

        return sb.toString();
    }

}