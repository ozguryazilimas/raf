package com.ozguryazilim.raf.utils;

import org.apache.deltaspike.core.api.config.ConfigResolver;

public class UrlUtils {

    private UrlUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getDocumentShareURL(String code) {
        return ConfigResolver.getPropertyValue("app.linkDomain") + "public/download.jsf?code=" + code;
    }

    public static String trimRafPaths(String path) {
        String logPath = path;

        //255 den daha uzun olan path lerde veritabanında ilgili sütuna yazılabilmesi için ortasını kesiyoruz.
        String longPathDivider = "...";
        int pathCharCountLimit = 255;
        int longPathPrefixOffset = 100;
        int longPathSuffixOffset = pathCharCountLimit - longPathPrefixOffset - longPathDivider.length();

        if (path.length() > pathCharCountLimit) {
            StringBuilder sb = new StringBuilder();
            sb.append(path, 0, longPathPrefixOffset)
                    .append(longPathDivider)
                    .append(path, path.length() - longPathSuffixOffset, path.length());

            logPath = sb.toString();
        }
        return logPath;
    }

}