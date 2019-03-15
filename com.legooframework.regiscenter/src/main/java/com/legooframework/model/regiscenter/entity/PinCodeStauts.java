package com.legooframework.model.regiscenter.entity;

import com.google.common.base.MoreObjects;

public enum PinCodeStauts {
    Init(0, "初始化"), Sending(1, "邮寄中"), Used(2, "已使用");

    int status;
    String desc;

    static PinCodeStauts paras(int val) {
        PinCodeStauts res;
        switch (val) {
            case 0:
                res = Init;
                break;
            case 1:
                res = Sending;
                break;
            case 2:
                res = Used;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    PinCodeStauts(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("desc", desc)
                .toString();
    }
}
