package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;

public enum UseScope {

    WxMsg(1, "微信"), SmsMsg(2, "短信");

    public static UseScope paras(int val) {
        UseScope res;
        switch (val) {
            case 1:
                res = WxMsg;
                break;
            case 2:
                res = SmsMsg;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    private int type;
    private String desc;

    UseScope(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("desc", desc)
                .toString();
    }
}
