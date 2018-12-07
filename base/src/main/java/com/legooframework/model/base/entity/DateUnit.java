package com.legooframework.model.base.entity;

public enum DateUnit {

    Hour('h', "小时"), Day('d', "天");

    private char unit;
    private String desc;

    DateUnit(char unit, String desc) {
        this.unit = unit;
        this.desc = desc;
    }

    public char getUnit() {
        return unit;
    }

    public String getDesc() {
        return desc;
    }
}

