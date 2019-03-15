package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;

import java.util.List;

public class RechargeBalanceList {

    private List<RechargeBalanceEntity> balances;
    private final long unusedNum;
    private final List<RechargeBalanceEntity> deductionList;

    RechargeBalanceList(List<RechargeBalanceEntity> balances) {
        this.balances = balances;
        this.unusedNum = balances.stream().mapToLong(RechargeBalanceEntity::getBalance).sum();
        this.deductionList = Lists.newArrayList();
    }

    public List<ChargeDetailEntity> deduction(CrmStoreEntity store, final String sms_batch_no, long be_used_size) {
        Preconditions.checkState(be_used_size > 0);
        Preconditions.checkState(unusedNum >= be_used_size, "可用短信数量%s低于待发送短信数量%s,扣费失败...", unusedNum, be_used_size);
        long temp_size = 0;
        long _size = be_used_size;
        List<ChargeDetailEntity> details = Lists.newArrayList();
        for (RechargeBalanceEntity balance : balances) {
            ChargeDetailEntity detail = new ChargeDetailEntity(sms_batch_no, store, balance);
            details.add(detail);
            this.deductionList.add(balance);
            temp_size = balance.deduction(_size);
            detail.setDeductionNum(temp_size);
            if (temp_size == _size) break;
            _size = _size - temp_size;
        }
        return details;
    }

    public List<RechargeBalanceEntity> getDeductionList() {
        return deductionList;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("unusedNum", unusedNum)
                .add("balances", balances)
                .toString();
    }
}
