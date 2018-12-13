package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;

public enum TaskStatus {

    Create(1, "创建"), Starting(2, "执行中"), Finished(3, "完成"),
    Stoped(4, "中止"), Canceled(5, "取消"), Expired(6, "过期"),
    Exceptioned(-1, "异常");

    private int status;
    private String desc;

    TaskStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    static TaskStatus paras(int val) {
        TaskStatus res;
        switch (val) {
            case 1:
                res = Create;
                break;
            case 2:
                res = Starting;
                break;
            case 3:
                res = Finished;
                break;
            case 4:
                res = Stoped;
                break;
            case 5:
                res = Canceled;
                break;
            case 6:
                res = Expired;
                break;
            case -1:
                res = Exceptioned;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("desc", desc)
                .toString();
    }
}
