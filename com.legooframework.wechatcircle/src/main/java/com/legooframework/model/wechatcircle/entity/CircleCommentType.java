package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;

public enum CircleCommentType {

    Liked(1, "点赞"), Comment(2, "评论");

    private final int type;
    private final String desc;

    CircleCommentType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    static CircleCommentType parse(int type) {
        switch (type) {
            case 1:
                return CircleCommentType.Liked;
            case 2:
                return CircleCommentType.Comment;
            default:
                throw new IllegalArgumentException(String.format("非法的入参取值...%s", type));
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
