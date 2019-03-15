package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class SaleRecordDetailEntity extends BaseEntity<Integer> {
    private final int googsCount, status;
    private final BigDecimal goodsPrice, salePrice, totalPrice;
    private final GoodsBaseEntity goodsBase;

    SaleRecordDetailEntity(Integer id, double salePrice, double goodsPrice,
                           int detailsStatus, int googsCount,
                           Integer goodsId, int goodsStatus, String funDesc, String goodsName, Integer companyId) {
        super(id);
        this.goodsPrice = new BigDecimal(goodsPrice);
        this.salePrice = new BigDecimal(salePrice);
        this.totalPrice = new BigDecimal(salePrice * googsCount);
        this.status = detailsStatus;
        this.googsCount = googsCount;
        this.goodsBase = new GoodsBaseEntity(goodsId, goodsStatus, funDesc, goodsName, companyId);
    }

    public int getGoogsCount() {
        return googsCount;
    }

    public int getStatus() {
        return status;
    }

    BigDecimal getTotalPrice() {
        return totalPrice;
    }

    Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("goodsCount", googsCount);
        params.put("salePrice", salePrice.doubleValue());
        params.put("totalPrice", totalPrice.doubleValue());
        params.put("goodsName", goodsBase.getName());
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SaleRecordDetailEntity that = (SaleRecordDetailEntity) o;
        return googsCount == that.googsCount &&
                status == that.status &&
                Objects.equals(goodsPrice, that.goodsPrice) &&
                Objects.equals(salePrice, that.salePrice) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(goodsBase, that.goodsBase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), googsCount, status, goodsPrice, salePrice, totalPrice, goodsBase);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("googsCount", googsCount)
                .add("status", status)
                .add("goodsPrice", goodsPrice)
                .add("salePrice", salePrice)
                .add("totalPrice", totalPrice)
                .add("goodsBase", goodsBase)
                .toString();
    }
}
