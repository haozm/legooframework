package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Objects;

public class Birthday implements Cloneable {

    private final LocalDate birthday;
    private final int type;
    private final DateTimeFormatter YMD_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final DateTimeFormatter MD_FORMATTER = DateTimeFormat.forPattern("MM-dd");

    Birthday(int type, String birthday) {
        if (birthday.length() == 5) {
            this.birthday = LocalDate.parse(birthday, MD_FORMATTER);
        } else {
            this.birthday = LocalDate.parse(birthday, YMD_FORMATTER);
        }

        this.type = type;
    }

    public boolean isLunarDate() {
        return this.type == 2;
    }

    public boolean isGregorianDate() {
        return this.type == 1;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Birthday birthday1 = (Birthday) o;
        return type == birthday1.type &&
                Objects.equals(birthday, birthday1.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(birthday, type);
    }

    public String toViewString() {
        return String.format("%s:%s", type, birthday.toString("yyyy-MM-dd"));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("birthday", birthday)
                .add("type", type)
                .toString();
    }
}
