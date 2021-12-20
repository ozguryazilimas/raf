package com.ozguryazilim.raf.ui.base;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RafReaderRegistery {

    private static final Logger LOG = LoggerFactory.getLogger(RafReaderRegistery.class);

    /**
     * Key: Simple Class Name, Value: Object Reference
     */
    private static final Map<String, RafReader> readers = new HashMap<>();

    /**
     * Key: MimeTypeRegex, Value: Simple Class Name
     */
    private static final Map<String, String> mimeMap = new HashMap<>();

    public static void register(String name, RafReader r) {
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        readers.put(name, r);
        mimeMap.put(r.mimeType(), name);

        LOG.info("Reader Registered : {}", name);
    }

    /**
     * Verilen mimeType'ı destekleyen panel'i döndürür.
     *
     * @param mimeType
     * @return
     */
    public static AbstractRafReaderPage getMimeTypePanel(String mimeType) {

        if (!Strings.isNullOrEmpty(mimeType)) {
            for (Map.Entry<String, String> ent : mimeMap.entrySet()) {
                Pattern pattern = Pattern.compile(ent.getKey());

                Matcher matcher = pattern.matcher(mimeType);
                if (matcher.matches()) {
                    return (AbstractRafReaderPage) BeanProvider.getContextualReference(ent.getValue(), true);
                }
            }
        }
        return null;
    }

    public static boolean isAnyReaderPageForGivenMimeType(String mimeType) {
        if (!Strings.isNullOrEmpty(mimeType)) {
            for (Map.Entry<String, String> ent : mimeMap.entrySet()) {
                Pattern pattern = Pattern.compile(ent.getKey());
                Matcher matcher = pattern.matcher(mimeType);
                if (matcher.matches()) return true;
            }
        }
        return false;
    }

}