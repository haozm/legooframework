package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;

public class AllocationRes {

    private final int type;
    private final Integer empId;
    private final double amount;

    AllocationRes(EmpDividedRuleEntity.Divided divided, Integer empId, double total) {
        this.type = divided.getType();
        this.empId = empId;
        this.amount = divided.allocation(total);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("empId", empId)
                .add("amount", amount)
                .toString();
    }
}
