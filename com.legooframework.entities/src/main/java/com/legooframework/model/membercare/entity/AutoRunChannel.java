package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;

public enum AutoRunChannel {

    WX_THEN_SMS(1, "微信优先"), SMS_ONLY(2, "仅短信"), WX_ONLY(3, "仅微信"), WX_AND_SMS(4, "微信短信全部");

    private final int channel;
    private final String desc;

    AutoRunChannel(int channel, String desc) {
        this.channel = channel;
        this.desc = desc;
    }

    public int getChannel() {
        return channel;
    }

    public String getDesc() {
        return desc;
    }

    public static AutoRunChannel parse(int channel) {
        AutoRunChannel runChannel;
        switch (channel) {
            case 1:
                runChannel = AutoRunChannel.WX_THEN_SMS;
                break;
            case 2:
                runChannel = AutoRunChannel.SMS_ONLY;
                break;
            case 3:
                runChannel = AutoRunChannel.WX_ONLY;
                break;
            case 4:
                runChannel = AutoRunChannel.WX_AND_SMS;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参 channel = %s", channel));
        }
        return runChannel;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("channel", channel)
                .add("desc", desc)
                .toString();
    }
}
