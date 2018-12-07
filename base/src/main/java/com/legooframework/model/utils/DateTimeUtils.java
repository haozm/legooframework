package com.legooframework.model.utils;


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

    public static void main(String[] args) {
        LocalDateTime a = DateTimeUtils.parseDef("2018-08-01 12:12:12");
        LocalDateTime b = DateTimeUtils.parseDef("2018-08-01 12:12:12");
        System.out.println(a.equals(b));
        LocalDateTime b2 = DateTimeUtils.parseDef("2018-08-01 12:12:13");
        System.out.println(a.isBefore(b2));
        LocalDateTime b3 = DateTimeUtils.parseDef("2018-08-01 12:12:11");
        System.out.println(a.isAfter(b3));
    }
}
