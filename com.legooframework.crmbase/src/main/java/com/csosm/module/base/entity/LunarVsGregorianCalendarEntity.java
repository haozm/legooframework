package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class LunarVsGregorianCalendarEntity extends BaseEntity<String> {

    private final String lunarCalendar;
    private final String lunarYearName;
    private final String lunarZodiac;
    private final String lunarMonthName;
    private final String lunarDayName;
    private final int weekDay;

    LunarVsGregorianCalendarEntity(String id, String lunarCalendar, String lunarYearName, String lunarZodiac,
                                   String lunarMonthName, String lunarDayName, int weekDay) {
        super(id);
        this.lunarCalendar = lunarCalendar;
        this.lunarYearName = lunarYearName;
        this.lunarZodiac = lunarZodiac;
        this.lunarMonthName = lunarMonthName;
        this.lunarDayName = lunarDayName;
        this.weekDay = weekDay;
    }

    public String getLunarCalendarStr(String format) {
        if (Strings.isNullOrEmpty(format) || StringUtils.equals("yyyyMMdd", format)) return lunarCalendar;
        try {
            Date dt = DateUtils.parseDate(lunarCalendar, "yyyyMMdd");
            return DateFormatUtils.format(dt, format);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getGregorianCalendarStr(String format) {
        if (Strings.isNullOrEmpty(format) || StringUtils.equals("yyyyMMdd", format)) return getId();
        try {
            Date dt = DateUtils.parseDate(getId(), "yyyyMMdd");
            return DateFormatUtils.format(dt, format);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LunarVsGregorianCalendarEntity that = (LunarVsGregorianCalendarEntity) o;
        return weekDay == that.weekDay &&
                Objects.equal(lunarCalendar, that.lunarCalendar) &&
                Objects.equal(lunarYearName, that.lunarYearName) &&
                Objects.equal(lunarZodiac, that.lunarZodiac) &&
                Objects.equal(lunarMonthName, that.lunarMonthName) &&
                Objects.equal(lunarDayName, that.lunarDayName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), lunarCalendar, lunarYearName, lunarZodiac,
                lunarMonthName, lunarDayName, weekDay);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("lunarCalendar", lunarCalendar)
                .add("lunarYearName", lunarYearName)
                .add("lunarZodiac", lunarZodiac)
                .add("lunarMonthName", lunarMonthName)
                .add("lunarDayName", lunarDayName)
                .add("weekDay", weekDay)
                .toString();
    }
}
