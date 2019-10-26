package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;

public enum CircleType {

    MixCircle(1, "朋友圈图文"), TextCircle(2, "纯文字"), SoftArticleCircle(3, "软文"), AudioCircle(4, "仅音频朋友圈"),
    VideoCircle(15, "视频朋友圈"), UNKNOWN(99, "未知类型");

    private final int type;
    private final String desc;

    CircleType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static CircleType parse(int type) {
        switch (type) {
            case 1:
                return CircleType.MixCircle;
            case 2:
                return CircleType.TextCircle;
            case 3:
                return CircleType.SoftArticleCircle;
            case 4:
                return CircleType.AudioCircle;
            case 15:
                return CircleType.VideoCircle;
            default:
                return CircleType.UNKNOWN;
        }
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
