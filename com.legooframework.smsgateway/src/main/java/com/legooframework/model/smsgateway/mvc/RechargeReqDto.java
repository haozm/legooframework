package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.smsgateway.entity.RechargeType;
import org.apache.commons.lang3.StringUtils;

public class RechargeReqDto {

    private final Integer companyId, storeId;
    private final String storeGroupId;
    private final RechargeType rechargeType;
    private final double unitPrice;
    private final String remarke;
    private final int rechargeAmount, totalQuantity;

    RechargeReqDto(Integer companyId, Integer storeId, String storeGroupId, RechargeType rechargeType, double unitPrice,
                   int rechargeAmount, int totalQuantity, String remarke) {
        this.companyId = companyId;
        this.storeId = storeId;
        this.storeGroupId = storeGroupId;
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
        return null != storeId;
    }

    public int getRechargeAmount() {
        return rechargeAmount;
    }

    public boolean isStoreGroupRange() {
        return null == storeId && StringUtils.isNotEmpty(storeGroupId);
    }

    public boolean isCompanyRange() {
        return null == storeId && StringUtils.isEmpty(storeGroupId);
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public String getStoreGroupId() {
        return storeGroupId;
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
                .add("storeGroupId", storeGroupId)
                .add("rechargeType", rechargeType)
                .add("unitPrice", unitPrice)
                .add("remarke", remarke)
                .toString();
    }
}
