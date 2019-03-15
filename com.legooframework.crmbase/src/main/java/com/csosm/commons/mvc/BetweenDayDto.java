package com.csosm.commons.mvc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

public class BetweenDayDto {

    private final Interval interval;
    private static DateTimeFormatter DATETIME_FORMARTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter YYYYMM_FORMARTER = DateTimeFormat.forPattern("yyyy-MM");

    private BetweenDayDto(String start, String end) {
        DateTime start_dt = DateTime.parse(String.format("%s 00:00:00", start), DATETIME_FORMARTER);
        DateTime end_dt = DateTime.parse(String.format("%s 00:00:00", end), DATETIME_FORMARTER).plusDays(1);
        this.interval = new Interval(start_dt, end_dt);
    }

    private DateTime getEndDay() {
        return this.interval.getEnd().plusSeconds(-1);
    }

    private BetweenDayDto(DateTime start, DateTime end) {
        this(start.toString("yyyy-MM-dd"), end.toString("yyyy-MM-dd"));
    }


    public static BetweenDayDto withStartAndEnd(String start, String end) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(start));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(end));
        return new BetweenDayDto(start, end);
    }

    public BetweenDayDto getAreaByMon(String yyyy_MM) {
        YearMonth ym_start = YearMonth.fromDateFields(interval.getStart().toDate());
        YearMonth ym_end = YearMonth.fromDateFields(getEndDay().toDate());
        YearMonth cursor = YearMonth.parse(yyyy_MM, YYYYMM_FORMARTER);
        Interval ym_interval = cursor.toInterval();
        if (ym_start.isEqual(cursor) && ym_end.isEqual(cursor)) return this;
        if (ym_start.isEqual(cursor) && ym_end.isAfter(cursor)) {
            return BetweenDayDto.withStartAndEnd(interval.getStart(), ym_interval.getEnd().plusDays(-1));
        }
        if (ym_start.isBefore(cursor) && ym_end.isAfter(cursor)) {
            return BetweenDayDto.withStartAndEnd(ym_interval.getStart(), ym_interval.getEnd().plusDays(-1));
        }
        if (ym_start.isBefore(cursor) && ym_end.isEqual(cursor)) {
            return BetweenDayDto.withStartAndEnd(ym_interval.getStart(), getEndDay());
        }
        throw new RuntimeException(String.format("%s can not exec getAreaByMon(%s)",
                this.toString(), yyyy_MM));
    }

    public BetweenDayDto getAreaByYear(int year) {
        int start_y = interval.getStart().getYear();
        int end_y = interval.getEnd().getYear();
        Preconditions.checkArgument(end_y >= year && start_y <= year,
                "非法的取值 %s，合理取值为[%s,%s]", year, start_y, end_y);
        if (start_y == year && end_y == year) return this;
        if (start_y == year && end_y > year)
            return BetweenDayDto.withStartAndEnd(interval.getStart().toString("yyyy-MM-dd"),
                    String.format("%s-12-31", year));
        if (start_y < year && end_y > year) return BetweenDayDto.withStartAndEnd(String.format("%s-01-01", year),
                String.format("%s-12-31", year));
        if (start_y < year && end_y == year) return BetweenDayDto.withStartAndEnd(String.format("%s-01-01", year),
                interval.getEnd().plusDays(-1).toString("yyyy-MM-dd"));
        throw new RuntimeException(String.format("%s can not exec getAreaByYear(%s)", this.toString(), year));
    }

    public static BetweenDayDto withStartAndEnd(DateTime start, DateTime end) {
        return new BetweenDayDto(start, end);
    }

    public static BetweenDayDto withStartEnd(String start_end) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(start_end));
        String[] dates = StringUtils.split(start_end, ',');
        return new BetweenDayDto(dates[0], dates[1]);
    }

    public static BetweenDayDto withToday() {
        return new BetweenDayDto(DateTime.now(), DateTime.now());
    }

    public Map<String, Object> toSqlDateStr(String mapKey) {
        Map<String, Object> map = Maps.newHashMap();
        map.put(Strings.isNullOrEmpty(mapKey) ? "start" : String.format("%s_start", mapKey),
                interval.getStart().toString("yyyy-MM-dd"));
        map.put(Strings.isNullOrEmpty(mapKey) ? "end" : String.format("%s_end", mapKey),
                interval.getEnd().toString("yyyy-MM-dd"));
        return map;
    }

    public String toStringBySpilt(String tag) {
        return String.format("%s%s%s", interval.getStart().toString("yyyy-MM-dd"),
                tag, getEndDay().toString("yyyy-MM-dd"));
    }

    public Map<String, Object> toDate(String mapKey) {
        Map<String, Object> map = Maps.newHashMap();
        map.put(Strings.isNullOrEmpty(mapKey) ? "start" : String.format("%s_start", mapKey),
                interval.getStart().toDate());
        map.put(Strings.isNullOrEmpty(mapKey) ? "end" : String.format("%s_end", mapKey),
                getEndDay().toDate());
        return map;
    }

    public Map<String, Object> toDateStr(String mapKey) {
        Map<String, Object> map = Maps.newHashMap();
        map.put(Strings.isNullOrEmpty(mapKey) ? "start" : String.format("%s_start", mapKey),
                interval.getStart().toString("yyyy-MM-dd"));
        map.put(Strings.isNullOrEmpty(mapKey) ? "end" : String.format("%s_end", mapKey),
                getEndDay().toString("yyyy-MM-dd"));
        return map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("start", interval.getStart().toString("yyyy-MM-dd HH:mm:ss"))
                .add("end", interval.getEnd().plusSeconds(-1).toString("yyyy-MM-dd HH:mm:ss"))
                .toString();
    }
}
