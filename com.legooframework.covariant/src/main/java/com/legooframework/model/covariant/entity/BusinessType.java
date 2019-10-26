package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;

public enum BusinessType {

    BIRTHDAYCARE(9, 1, "生日关怀"), REORDERPLAN(7, 2, "返单计划"), HOLIDAYCARE(21, 2, "节日关怀"),
    NINETYPLAN(6, 1, "90感动计划"), QUICK_APARTMENT(11, 2, "回访计划"), TYPE_CUSTOM(13, 2, "自定义"),
    NEW_SALE_RECORD(1, 2, "新销售单");

    private int value, smsChannel;
    private String desc;

    public static BusinessType paras(int val) {
        BusinessType res;
        switch (val) {
            case 6:
                res = NINETYPLAN;
                break;
            case 7:
                res = REORDERPLAN;
                break;
            case 9:
                res = BIRTHDAYCARE;
                break;
            case 11:
                res = QUICK_APARTMENT;
                break;
            case 13:
                res = TYPE_CUSTOM;
                break;
            case 21:
                res = HOLIDAYCARE;
                break;
            case 1:
                res = NEW_SALE_RECORD;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参....%d", val));
        }
        return res;
    }

    BusinessType(int value, int smsChannel, String desc) {
        this.value = value;
        this.smsChannel = smsChannel;
        this.desc = desc;
    }

    public int getSmsChannel() {
        return smsChannel;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("smsChannel", smsChannel)
                .add("desc", desc)
                .toString();
    }
}
