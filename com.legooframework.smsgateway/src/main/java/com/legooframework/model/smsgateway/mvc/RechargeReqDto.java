package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.smsgateway.entity.RechargeType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

public class RechargeReqDto {

    private final Integer companyId, storeId;
    private final Collection<Integer> storeIds;
    private final RechargeType rechargeType;
    private final double unitPrice;
    private final String remarke;
    private final int rechargeAmount, totalQuantity;

    RechargeReqDto(Integer companyId, Integer storeId, Collection<Integer> storeIds, RechargeType rechargeType, double unitPrice,
                   int rechargeAmount, int totalQuantity, String remarke) {
        this.companyId = companyId;
        this.storeId = storeId == null ? -1 : storeId;
        this.storeIds = storeIds;
        this.rechargeType = rechargeType;
        this.unitPrice = unitPrice;
        this.remarke = remarke;
        this.totalQuantity = totalQuantity;
        if (RechargeType.FreeCharge != rechargeType) {
            Preconditions.checkArgument(rechargeAmount > 0, "非法的rechargeAmount=%s 取值..", rechargeAmount);
            this.rechargeAmount = rechargeAmount * 100;
        } else {
            this.rechargeAmount = 0;
        }
    }

    public boolean isFreeCharge() {
        return RechargeType.FreeCharge == rechargeType;
    }

    public int getTotalQuantity() {
        Preconditions.checkArgument(totalQuantity > 0, "短信数量必须>0,非法的入参 %s", totalQuantity);
        return totalQuantity;
    }

    public boolean hasUnitPrice() {
        return 0.0D != unitPrice;
    }

    public boolean isStoreRange() {
        return -1 != storeId;
    }

    public int getRechargeAmount() {
        return rechargeAmount;
    }

    public boolean isStoreGroupRange() {
        return CollectionUtils.isNotEmpty(this.storeIds);
    }

    public boolean isCompanyRange() {
        return -1 == storeId && CollectionUtils.isEmpty(this.storeIds);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Collection<Integer> getStoreIds() {
        return storeIds;
    }

    public RechargeType getRechargeType() {
        return rechargeType;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getRemarke() {
        return remarke;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("storeIds", storeIds)
                .add("rechargeType", rechargeType)
                .add("unitPrice", unitPrice)
                .add("remarke", remarke)
                .toString();
    }
}
