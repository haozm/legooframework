package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

public enum SendStatus {

    Transport(0, "入库提交中"), Storage(1, "等待发送"), Sending(2, "短信网关发送中"), Sended(3, "发送网关完毕，等待反馈"),
    Error(7, "发送异常"), Concaled(9, "取消发送");

    private final int status;
    private final String desc;

    SendStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    static SendStatus paras(int val) {
        SendStatus res;
        switch (val) {
            case 0:
                res = SendStatus.Transport;
                break;
            case 1:
                res = SendStatus.Storage;
                break;
            case 2:
                res = SendStatus.Sending;
                break;
            case 3:
                res = SendStatus.Sended;
                break;
            case 7:
                res = SendStatus.Error;
                break;
            case 9:
                res = SendStatus.Concaled;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("desc", desc)
                .toString();
    }
}
