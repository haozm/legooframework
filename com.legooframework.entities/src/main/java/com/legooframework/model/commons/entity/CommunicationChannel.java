package com.legooframework.model.commons.entity;

import com.google.common.base.MoreObjects;

public enum CommunicationChannel {

    SMS(1, "短信"), WEIXIN(2, "微信模式");

    private final int channel;
    private final String desc;

    public int getChannel() {
        return channel;
    }

    public String getDesc() {
        return desc;
    }

    public static CommunicationChannel paras(int val) {
        CommunicationChannel res;
        switch (val) {
            case 1:
                res = CommunicationChannel.SMS;
                break;
            case 2:
                res = CommunicationChannel.WEIXIN;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参:%s", val));
        }
        return res;
    }

    CommunicationChannel(int channel, String desc) {
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
