package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

/**
 * 充值范围
 */
public enum RechargeScope {

    Company(1, "公司范围"), StoreGroup(2, "组织范围"), Store(3, "门店范围");

    private final int scope;
    private final String desc;

    RechargeScope(int scope, String desc) {
        this.scope = scope;
        this.desc = desc;
    }

    static RechargeScope paras(int val) {
        RechargeScope res;
        switch (val) {
            case 1:
                res = RechargeScope.Company;
                break;
            case 2:
                res = RechargeScope.StoreGroup;
                break;
            case 3:
                res = RechargeScope.Store;
                break;
            default:
                throw new IllegalArgumentException("非法的入参....");
        }
        return res;
    }

    public int getScope() {
        return scope;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scope", scope)
                .add("desc", desc)
                .toString();
    }
}
