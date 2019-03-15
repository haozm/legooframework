package com.legooframework.model.rfm.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Range;

import java.util.Map;

public abstract class AbstractVal {
    Range<Integer> range1st;
    Range<Integer> range2nd;
    Range<Integer> range3rd;
    Range<Integer> range4th;
    Range<Integer> range5lv;


    public Range<Integer> getRange1st() {
        return range1st;
    }

    public Range<Integer> getRange2nd() {
        return range2nd;
    }

    public Range<Integer> getRange3rd() {
        return range3rd;
    }

    public Range<Integer> getRange4th() {
        return range4th;
    }

    public Range<Integer> getRange5lv() {
        return range5lv;
    }

    abstract Map<String, Object> toMap();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractVal)) return false;
        AbstractVal that = (AbstractVal) o;
        return Objects.equal(range1st, that.range1st) &&
                Objects.equal(range2nd, that.range2nd) &&
                Objects.equal(range3rd, that.range3rd) &&
                Objects.equal(range4th, that.range4th) &&
                Objects.equal(range5lv, that.range5lv);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(range1st, range2nd, range3rd, range4th, range5lv);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("range1st", range1st)
                .add("range2nd", range2nd)
                .add("range3rd", range3rd)
                .add("range4th", range4th)
                .add("range5lv", range5lv)
                .toString();
    }
}
