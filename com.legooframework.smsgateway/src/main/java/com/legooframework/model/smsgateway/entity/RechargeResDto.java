package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

public class RechargeResDto {
    
    private final RechargeDetailEntity rechargeDetail;
    private int totalQuantity;

    RechargeResDto(RechargeDetailEntity rechargeDetail) {
        this.rechargeDetail = rechargeDetail;
        this.totalQuantity = rechargeDetail.getTotalQuantity();
    }

    RechargeDetailEntity getRechargeDetail() {
        return rechargeDetail;
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
                .add("rechargeDetail", rechargeDetail)
                .add("totalQuantity", totalQuantity)
                .toString();
    }
}
