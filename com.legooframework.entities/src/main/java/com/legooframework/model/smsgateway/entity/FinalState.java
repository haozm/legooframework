package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

public enum FinalState {

    WAITING(0, "待发送状态"),
    DELIVRD(1, "发送成功"),
    UNDELIV(2, "发送失败"),
    SENDEDERROR(97, "发送异常"),
    SENDEDOK(98, "发送完毕"),
    SENDING(99, "短信发送中");

    private final int state;
    private final String desc;

    FinalState(int state, String desc) {
        this.state = state;
        this.desc = desc;
    }

    public static FinalState paras(int val) {
        FinalState res;
        switch (val) {
            case 0:
                res = FinalState.WAITING;
                break;
            case 1:
                res = FinalState.DELIVRD;
                break;
            case 2:
                res = FinalState.UNDELIV;
                break;
            case 99:
                res = FinalState.SENDING;
                break;
            case 98:
                res = FinalState.SENDEDOK;
                break;
            case 97:
                res = FinalState.SENDEDERROR;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的FinalState取值....%s", val));
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
