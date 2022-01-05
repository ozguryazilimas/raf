package com.ozguryazilim.raf;

import java.io.Serializable;
import java.util.Locale;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 *
 * @author evren.cengiz
 */
@WindowScoped
@Named
public class CaseSensitiveSearchService implements Serializable {

    private boolean caseSensitiveSearchOptionEnabled = "true".equals(ConfigResolver.getPropertyValue("caseSensitiveSearchOptionEnabled", "false"));

    private Locale searchLocale = Locale.forLanguageTag(ConfigResolver.getPropertyValue("searchLocale", "tr-TR"));

    public boolean isCaseSensitiveSearchOptionEnabled() {
        return caseSensitiveSearchOptionEnabled;
    }

    public Locale getSearchLocale() {
        return searchLocale;
    }

    public boolean caseSensitiveStringEqual(String s, String s1) {
        return caseSensitiveSearchOptionEnabled ? s.equals(s1) : s.toUpperCase(searchLocale).equals(s1.toUpperCase(searchLocale));
    }
}
