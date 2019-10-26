package com.legooframework.model.statistical.entity;

public enum DateRange {
    TODAY("今天"), WEEK("本周"), BEFWEEK("上周"), MONTH("本月"), BEFMONTH("上月"), LAST3MONTH("近三个月"),
    JIDU("本季度"), BEFJIDU("上季度"), HALFYEAR("近半年"), YEAR("本年"), CUSTOMIZE("自定义");
    private final String desc;

    DateRange(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
