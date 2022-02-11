package com.ozguryazilim.raf.utils;

import com.ozguryazilim.telve.auth.Identity;
import org.omnifaces.el.functions.Strings;

public class IdentityUtils {

    private IdentityUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getPrettyNameSurname(Identity identity) {
        String name = Strings.capitalize(identity.getUserInfo().getFirstName());
        String surname = Strings.capitalize(identity.getUserInfo().getLastName());
        return name + " " + surname;
    }
}