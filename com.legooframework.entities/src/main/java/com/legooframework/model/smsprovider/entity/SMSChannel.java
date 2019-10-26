package com.legooframework.model.smsprovider.entity;

import com.google.common.base.MoreObjects;

public enum SMSChannel {

    TradeChannel(1, "行业通道"), MarketChannel(2, "营销通道");

    private final int channel;
    private final String desc;

    SMSChannel(int channel, String desc) {
        this.channel = channel;
        this.desc = desc;
    }

    public static SMSChannel paras(int val) {
        SMSChannel res;
        switch (val) {
            case 1:
                res = SMSChannel.TradeChannel;
                break;
            case 2:
                res = SMSChannel.MarketChannel;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    public int getChannel() {
        return channel;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("channel", channel)
                .add("desc", desc)
                .toString();
    }
}
