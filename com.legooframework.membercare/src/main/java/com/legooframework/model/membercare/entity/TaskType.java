package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;

public enum TaskType {

    BrithDay(1, "brithDayJob"), Touche90(90, "touch90Job"), FestivalDay(3, "festivalDayJob");

    private int value;
    private String desc;

    TaskType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static TaskType parse(int val) {
        TaskType res;
        switch (val) {
            case 1:
                res = BrithDay;
                break;
            case 90:
                res = Touche90;
                break;
            case 3:
                res = FestivalDay;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("desc", desc)
                .toString();
    }
}
