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

    public static String trimRafPaths(String path) {
        return trimRafPaths(path, 255, 100);
    }

    public static String trimRafPaths(String path, int limit, int prefixOffset) {
        String logPath = path;

        //255 den daha uzun olan path lerde veritabanında ilgili sütuna yazılabilmesi için ortasını kesiyoruz.
        String longPathDivider = "...";
        int pathCharCountLimit = limit;
        int longPathPrefixOffset = prefixOffset;
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