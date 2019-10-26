package com.legooframework.model.autotask.entity;

import com.google.common.base.MoreObjects;

public enum DelayType {

    NO_DELAY(0, "无延时"), POSTPONE_DELAY(1, "顺延执行"), TIMING_DELAY(2, "顺延定时执行");

    private int value;
    private String desc;

    public static boolean isDelay4Job(DelayType delayType) {
        return POSTPONE_DELAY == delayType || TIMING_DELAY == delayType;
    }

    public static DelayType paras(int val) {
        DelayType res;
        switch (val) {
            case 0:
                res = NO_DELAY;
                break;
            case 1:
                res = POSTPONE_DELAY;
                break;
            case 2:
                res = TIMING_DELAY;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参....%d", val));
        }
        return res;
    }

    DelayType(int value, String desc) {
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
