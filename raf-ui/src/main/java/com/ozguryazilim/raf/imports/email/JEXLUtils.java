package com.ozguryazilim.raf.imports.email;

import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.StringUtils;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JEXLUtils {

    public static Locale getLocale(String language, String country) {
        return new Locale(language, country);
    }

    public static Date dateAddMonth(Date date, int count) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, count);
        return cal.getTime();
    }

    public static String getMonthDisplayName(int idx, Locale locale) {
        return Month.of(idx).getDisplayName(TextStyle.FULL_STANDALONE, locale);
    }

    public static String getFromName(InternetAddress from) {
        String personal = from.getPersonal();
        if (StringUtils.isNotBlank(personal)) {
            return personal;
        }

        String address = from.getAddress();
        String[] addressNameParts =  address.split("@");

        if (addressNameParts.length > 0 && StringUtils.isNotBlank(addressNameParts[0])) {
            return addressNameParts[0];
        }
        return address;
    }
}
