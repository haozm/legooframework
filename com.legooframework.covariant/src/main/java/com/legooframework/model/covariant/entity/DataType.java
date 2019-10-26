package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;

public enum DataType {

    JSON(0, "JSON数据"), KEY_VALUE(1, "K_V数据");

    private int value;
    private String desc;

    public static DataType paras(int val) {
        DataType res;
        switch (val) {
            case 0:
                res = JSON;
                break;
            case 1:
                res = KEY_VALUE;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参....%d", val));
        }
        return res;
    }

    DataType(int value, String desc) {
        this.value = value;
        this.desc = desc;
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
                .add("desc", desc)
                .toString();
    }
}
