package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

public enum SendStatus {

    SMS4Inited(0, "消息入库完成"),
    SMS4Storage(1, "消息等待发送"),
    SMS4Sending(2, "消息发送中"),
    SendedGateWay(3, "消息提交完成"),
    SMS4SendError(4, "消息发送异常"),
    SMS4InitError(7, "消息生成异常"),
    SMS4Concaled(9, "消息取消发送");

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

    public static SendStatus paras(int val) {
        SendStatus res;
        switch (val) {
            case 0:
                res = SendStatus.SMS4Inited;
                break;
            case 1:
                res = SendStatus.SMS4Storage;
                break;
            case 2:
                res = SendStatus.SMS4Sending;
                break;
            case 3:
                res = SendStatus.SendedGateWay;
                break;
            case 4:
                res = SendStatus.SMS4SendError;
                break;
            case 7:
                res = SendStatus.SMS4InitError;
                break;
            case 9:
                res = SendStatus.SMS4Concaled;
                break;
            default:
                throw new IllegalArgumentException(String.format("非法的入参...%s,无法匹配 SendStatus 对象", val));
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
