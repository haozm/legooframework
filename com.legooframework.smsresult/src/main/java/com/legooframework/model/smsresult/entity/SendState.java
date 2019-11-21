package com.legooframework.model.smsresult.entity;

import com.google.common.base.MoreObjects;

public enum SendState {

    WAITING(0, "等待发送平台"), SENDED(1, "发送平台完成"), ERROR(2, "发送平台失败"), SENDING(99, "平台发送中");

    private final int state;
    private final String desc;

    SendState(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static SendState paras(int val) {
        SendState res;
        switch (val) {
            case 0:
                res = SendState.WAITING;
                break;
            case 1:
                res = SendState.SENDED;
                break;
            case 2:
                res = SendState.ERROR;
                break;
            case 99:
                res = SendState.SENDING;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的SendState取值....%s", val));
        }
        return res;
    }

    public int getState() {
        return state;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("state", state)
                .add("desc", desc)
                .toString();
    }
}
