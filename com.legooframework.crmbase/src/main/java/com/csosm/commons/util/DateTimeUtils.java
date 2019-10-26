package com.csosm.commons.util;


import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class DateTimeUtils {

    private static DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime parseDef(String datatime) {
        return LocalDateTime.parse(datatime, YYYY_MM_DD_HH_MM_SS);
    }

    public static String format(LocalDateTime localDateTime) {
        return localDateTime.toString(YYYY_MM_DD_HH_MM_SS);
    }

}
