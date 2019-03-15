package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

public class RechargeRes {
    private final String rechargeId;
    private int totalQuantity;

    RechargeRes(RechargeDetailEntity rechargeDetail) {
        this.rechargeId = rechargeDetail.getId();
        this.totalQuantity = rechargeDetail.getTotalQuantity();
    }

    RechargeRes(String rechargeId, int totalQuantity) {
        this.rechargeId = rechargeId;
        this.totalQuantity = totalQuantity;
    }


    public String getRechargeId() {
        return rechargeId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    void subtraction(int quantity) {
        this.totalQuantity -= quantity;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rechargeId", rechargeId)
                .add("totalQuantity", totalQuantity)
                .toString();
    }
}
