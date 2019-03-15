package com.csosm.module.base.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Date;

public class Birthday implements Cloneable {

    // 公历生日
    private Date gregorianDate;
    // 农历生日
    private Date lunarDate;

    private Integer calendarType = 1;

    public Birthday(Date gregorianDate, Date lunarDate, Integer calendarType) {
        super();
        this.gregorianDate = gregorianDate;
        this.lunarDate = lunarDate;
        if (calendarType != null)
            this.calendarType = calendarType;
    }

    public void setCalendarType(Integer calendarType) {
        this.calendarType = calendarType;
    }

    public Birthday modify(Date gregorianDate, Date lunarDate, Integer calendarType) {
        Birthday clone = null;
        try {
            clone = (Birthday) this.clone();
            clone.gregorianDate = gregorianDate;
            clone.lunarDate = lunarDate;
            clone.calendarType = calendarType;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("复制修改Birthday发生异常");
        }
        return clone;

    }

    public void setGregorianDate(Date gregorianDate) {
        this.gregorianDate = gregorianDate;
    }

    public void setLunarDate(Date lunarDate) {
        this.lunarDate = lunarDate;
    }

    public boolean isLunarDate() {
        return this.calendarType == 2;
    }

    public Integer getCalendarType() {
        return calendarType;
    }

    public boolean isGregorianDate() {
        return this.calendarType == 1;
    }

    public Date getGregorianDate() {
        return gregorianDate;
    }

    public Date getLunarDate() {
        return lunarDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Birthday birthday = (Birthday) o;
        return Objects.equal(gregorianDate, birthday.gregorianDate) && Objects.equal(lunarDate, birthday.lunarDate)
                && Objects.equal(calendarType, birthday.calendarType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(gregorianDate, lunarDate, calendarType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("gregorianDate", gregorianDate).add("lunarDate", lunarDate)
                .add("calendarType", calendarType).toString();
    }
}
