package com.legooframework.model.commons.entity;

import com.google.common.base.MoreObjects;

public enum SendChannel {

    SMS(1, "短信"), WEIXIN(2, "微信");

    private final int channel;
    private final String desc;

    public int getChannel() {
        return channel;
    }

    public String getDesc() {
        return desc;
    }

    public static SendChannel paras(int val) {
        SendChannel res;
        switch (val) {
            case 1:
                res = SendChannel.SMS;
                break;
            case 2:
                res = SendChannel.WEIXIN;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参:%s", val));
        }
        return res;
    }

    SendChannel(int channel, String desc) {
        this.channel = channel;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("channel", channel)
                .add("desc", desc)
                .toString();
    }
}
