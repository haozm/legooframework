package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;

public enum SendChannel {

    SMS(0, "短信发送"), WECHAT(1, "微信发送"), WECHAT_GZH(2, "公众号发送"),
    OFFLINE(3, "线下更进"), CALLPHONE(4, "电话跟进"), WX_SMS(10, "微信优先"), CANCEL(9, "取消跟进");

    private int value;
    private String desc;

    public static SendChannel paras(int val) {
        SendChannel res;
        switch (val) {
            case 0:
                res = SMS;
                break;
            case 1:
                res = WECHAT;
                break;
            case 2:
                res = WECHAT_GZH;
                break;
            case 3:
                res = OFFLINE;
                break;
            case 4:
                res = CALLPHONE;
                break;
            case 10:
                res = WX_SMS;
                break;
            case 9:
                res = CANCEL;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参....%d", val));
        }
        return res;
    }

    SendChannel(int value, String desc) {
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
