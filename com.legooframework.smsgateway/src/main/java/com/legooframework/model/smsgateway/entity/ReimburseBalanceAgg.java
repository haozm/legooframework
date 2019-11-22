package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Optional;

public class ReimburseBalanceAgg {

    private List<RechargeBalanceEntity> rechargeBalances;
    private ReimburseResDto reimburseDto;
    private RechargeDetailEntity rechargeDetail;
    private RechargeBalanceEntity balance;

    ReimburseBalanceAgg(List<RechargeBalanceEntity> rechargeBalances) {
        this.rechargeBalances = rechargeBalances;
    }

    public void reimburse(ReimburseResDto reimburse) {
        this.reimburseDto = reimburse;
        balance = rechargeBalances.get(0);
        if (RechargeScope.Store == balance.getRechargeScope()) {
            rechargeDetail = RechargeDetailEntity.reimburseByStore(reimburse);
            balance.addBalance(reimburse.getTotalSmsCount());
        } else if (RechargeScope.StoreGroup == balance.getRechargeScope()) {
            rechargeDetail = RechargeDetailEntity.reimburseByStoreGroup(balance, reimburse);
            balance.addBalance(reimburse.getTotalSmsCount());
        } else {
            rechargeDetail = RechargeDetailEntity.reimburseByCompany(reimburse);
            balance.addBalance(reimburse.getTotalSmsCount());
        }
    }

    public Optional<RechargeDetailEntity> getRechargeDetail() {
        return Optional.ofNullable(rechargeDetail);
    }

    public Optional<RechargeBalanceEntity> getBalance() {
        return Optional.ofNullable(balance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ReimburseDto", reimburseDto)
                .add("rechargeDetail", rechargeDetail)
                .add("balance", balance)
                .toString();
    }
}
