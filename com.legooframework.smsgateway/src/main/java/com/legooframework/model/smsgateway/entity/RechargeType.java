package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

public enum RechargeType {

    Recharge(1, "充值"), Precharge(2, "预充值"), FreeCharge(3, "免费充值"), Deduction(4, "抵扣"),
    Reimburse(9, "系统退款");

    private final int type;
    private final String desc;

    RechargeType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    static RechargeType paras(int val) {
        RechargeType res;
        switch (val) {
            case 1:
                res = RechargeType.Recharge;
                break;
            case 2:
                res = RechargeType.Precharge;
                break;
            case 3:
                res = RechargeType.FreeCharge;
                break;
            case 4:
                res = RechargeType.Deduction;
                break;
            case 9:
                res = RechargeType.Reimburse;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("desc", desc)
                .toString();
    }
}
