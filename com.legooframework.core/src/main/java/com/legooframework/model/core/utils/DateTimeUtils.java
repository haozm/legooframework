package com.legooframework.model.core.utils;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public abstract class DateTimeUtils {

    private static DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static DateTimeFormatter YYYY_MM_DD = DateTimeFormat.forPattern("yyyy-MM-dd");
    public final static DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");

    public static LocalDateTime parseDef(String datatime) {
        return LocalDateTime.parse(datatime, YYYY_MM_DD_HH_MM_SS);
    }

    public static DateTime parseDateTime(String datatime) {
        return DateTime.parse(datatime, YYYY_MM_DD_HH_MM_SS);
    }

    public static LocalDateTime parseYYYYMMDDHHMMSS(String datatime) {
        return LocalDateTime.parse(datatime, YYYYMMDDHHMMSS);
    }

    public static LocalDate parseShortYYYYMMDD(String datatime) {
        return LocalDate.parse(datatime, YYYYMMDD);
    }


    public static LocalDate parseYYYYMMDD(String datatime) {
        return LocalDate.parse(datatime, YYYY_MM_DD);
    }

    public static String format(LocalDateTime localDateTime) {
        return localDateTime.toString(YYYY_MM_DD_HH_MM_SS);
    }

    public static long toUnixTimetamp(String yyyyMMddHHmmss) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            return sdf.parse(yyyyMMddHHmmss).getTime() / 1000;
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("toUnixTimetamp(%s)异常...", yyyyMMddHHmmss), e);
        }
    }

    public static Date unixToDate(long dateTime) {
        return new Date(dateTime * 1000);
    }

    public static LocalDateTime unixToLocalDateTime(long dateTime) {
        Date dt = new Date(dateTime * 1000);
        return LocalDateTime.fromDateFields(dt);
    }

    public static Map<String, Object> wrappDatePeriod(LocalDate start, LocalDate end) {
        Map<String, Object> params = Maps.newHashMap();
        if (start.getYear() == end.getYear()) {
            params.put("thisYear", start.getYear());
            params.put("thisStart", start.toString("yyyy-MM-dd"));
            params.put("thisStartTime", start.toString("yyyy-MM-dd 00:00:00"));
            params.put("thisEnd", end.toString("yyyy-MM-dd"));
            params.put("thisEndTime", end.toString("yyyy-MM-dd 23:59:59"));
        } else {
            params.put("thisYear", start.getYear());
            params.put("thisStart", start.toString("yyyy-MM-dd"));
            params.put("thisStartTime", start.toString("yyyy-MM-dd 00:00:00"));
            LocalDate _temp = start.dayOfYear().withMaximumValue();
            params.put("thisEnd", _temp.toString("yyyy-MM-dd"));
            params.put("thisEndTime", _temp.toString("yyyy-MM-dd 23:59:59"));

            params.put("nextYear", end.getYear());
            _temp = end.dayOfYear().withMinimumValue();
            params.put("nextStart", _temp.toString("yyyy-MM-dd"));
            params.put("nextStartTime", _temp.toString("yyyy-MM-dd 00:00:00"));
            params.put("nextEnd", end.toString("yyyy-MM-dd"));
            params.put("nextEndTime", end.toString("yyyy-MM-dd 23:59:59"));
        }
        return params;
    }

    public static Map<String, Object> wrappDatePeriod(String start, String end) {
        LocalDate startTime = parseYYYYMMDD(start);
        LocalDate endTime = parseYYYYMMDD(end);
        return wrappDatePeriod(startTime, endTime);
    }

}
