package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;

public enum TemplateUseScope {

    WxMsg(1, "微信"), SmsMsg(2, "短信");

    static TemplateUseScope paras(int val) {
        TemplateUseScope res;
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

    TemplateUseScope(int type, String desc) {
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
