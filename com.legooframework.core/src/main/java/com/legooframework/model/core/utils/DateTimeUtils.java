package com.legooframework.model.core.utils;


import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class DateTimeUtils {

    private static DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static DateTimeFormatter YYYY_MM_DD = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static LocalDateTime parseDef(String datatime) {
        return LocalDateTime.parse(datatime, YYYY_MM_DD_HH_MM_SS);
    }

    public static LocalDateTime parseYYYYMMDDHHMMSS(String datatime) {
        return LocalDateTime.parse(datatime, YYYYMMDDHHMMSS);
    }

    public static LocalDate parseYYYYMMDD(String datatime) {
        return LocalDate.parse(datatime, YYYY_MM_DD);
    }

    public static String format(LocalDateTime localDateTime) {
        return localDateTime.toString(YYYY_MM_DD_HH_MM_SS);
    }

}
