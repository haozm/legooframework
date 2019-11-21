package com.legooframework.model.smsresult.entity;

import com.google.common.base.MoreObjects;

public enum FinalState {

    WAITING(0, "等待状态返回"),
    DELIVRD(1, "下发短信成功"),
    UNDELIV(2, "下发短息失败");
    
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
