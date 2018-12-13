package com.legooframework.model.dict.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.csv.CSVRecord;

public class LunarIndexEntity extends BaseEntity<Integer> {

    private final int lunarVal;
    private final String zodiac;
    private final String monthOfLunar;
    private final String mmddOfLunar;
    private final String dayOfLunar;
    private final int dayOfWeek;

    LunarIndexEntity(CSVRecord record) {
        super(Integer.valueOf(record.get(0)));
        String val = record.get(1);
        this.lunarVal = Integer.valueOf(val);
        this.mmddOfLunar = val.substring(4);
        this.zodiac = record.get(3);
        this.monthOfLunar = record.get(4);
        this.dayOfLunar = record.get(5);
        this.dayOfWeek = Integer.valueOf(record.get(6));
    }

    public int getLunarVal() {
        return lunarVal;
    }

    public String getZodiac() {
        return zodiac;
    }

    public String getMonthOfLunar() {
        return monthOfLunar;
    }

    public String getDayOfLunar() {
        return dayOfLunar;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getMmddOfLunar() {
        return mmddOfLunar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LunarIndexEntity that = (LunarIndexEntity) o;
        return lunarVal == that.lunarVal &&
                dayOfWeek == that.dayOfWeek &&
                Objects.equal(zodiac, that.zodiac) &&
                Objects.equal(monthOfLunar, that.monthOfLunar) &&
                Objects.equal(dayOfLunar, that.dayOfLunar);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), lunarVal, zodiac, monthOfLunar, dayOfLunar, dayOfWeek);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("lunarVal", lunarVal)
                .add("zodiac", zodiac)
                .add("monthOfLunar", monthOfLunar)
                .add("dayOfLunar", dayOfLunar)
                .add("dayOfWeek", dayOfWeek)
                .toString();
    }
}
