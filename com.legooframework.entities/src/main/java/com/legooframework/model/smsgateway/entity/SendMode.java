package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

public enum SendMode {

    AutoJob(0, "自动任务"), ManualSingle(1, "手动单发"), ManualBatch(2, "手动群发");

    private final int mode;
    private final String desc;

    SendMode(int mode, String desc) {
        this.mode = mode;
        this.desc = desc;
    }

    public int getMode() {
        return mode;
    }

    public String getDesc() {
        return desc;
    }

    public static SendMode paras(int val) {
        SendMode res;
        switch (val) {
            case 0:
                res = SendMode.AutoJob;
                break;
            case 1:
                res = SendMode.ManualSingle;
                break;
            case 2:
                res = SendMode.ManualBatch;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mode", mode)
                .add("desc", desc)
                .toString();
    }
}
