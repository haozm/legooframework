package com.legooframework.model.salesrecords.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SaleGoodsEntity extends BaseEntity<String> {

    private final String goodsName, oldGoodsId;
    private final double retailProce;

    SaleGoodsEntity(String oldGoodsId, Integer companyId, ResultSet res) {
        super(oldGoodsId, companyId.longValue(), -1L);
        try {
            this.goodsName = ResultSetUtil.getString(res, "goodsName");
            this.oldGoodsId = ResultSetUtil.getString(res, "oldGoodsId");
            this.retailProce = ResultSetUtil.getOptObject(res, "retailProce", Double.class).orElse(0.0D);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SaleGoodsEntity has SQLException", e);
        }
    }

    public String getGoodsName() {
        return goodsName;
    }

    public String getOldGoodsId() {
        return oldGoodsId;
    }

    public double getRetailProce() {
        return retailProce;
    }

    public Integer getCompanyId() {
        return this.getTenantId().intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SaleGoodsEntity)) return false;
        SaleGoodsEntity that = (SaleGoodsEntity) o;
        return Double.compare(that.retailProce, retailProce) == 0 &&
                Objects.equal(goodsName, that.goodsName) &&
                Objects.equal(getTenantId(), that.getTenantId()) &&
                Objects.equal(oldGoodsId, that.oldGoodsId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(goodsName, oldGoodsId, retailProce, getTenantId());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("oldGoodsId", oldGoodsId)
                .add("goodsName", goodsName)
                .add("retailProce", retailProce)
                .add("companyId", getTenantId())
                .toString();
    }
}
