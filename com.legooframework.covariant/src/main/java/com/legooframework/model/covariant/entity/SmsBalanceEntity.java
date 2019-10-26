package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class SmsBalanceEntity extends BaseEntity<Integer> {

    private int count, type;
    private double price;
    private final Integer orgId, storeId;

    void setType(int type) {
        this.type = type;
    }

    boolean isOrgBalance() {
        return this.type == 1;
    }

    boolean isStoreBalance() {
        return this.type == 3;
    }

    SmsBalanceEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.orgId = ResultSetUtil.getOptObject(res, "organization_id", Integer.class).orElse(null);
            this.storeId = ResultSetUtil.getOptObject(res, "store_id", Integer.class).orElse(null);
            this.price = res.getBigDecimal("smsPrice") == null ? 0.0D : res.getBigDecimal("smsPrice").doubleValue();
            this.count = ResultSetUtil.getOptObject(res, "smsCount", Integer.class).orElse(0);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SmsBalanceEntity has SQLException", e);
        }
    }

    int getCount() {
        return count;
    }

    boolean hasBalance() {
        return this.count > 0;
    }

    int billing(int smsCount) {
        if (isStoreBalance()) {
            Preconditions.checkState(this.count > 0, "当前 %s 无可支配的短信数量...", this.toString());
            if (this.count >= smsCount) {
                this.count = this.count - smsCount;
                return 0;
            } else {
                int res = smsCount - this.count;
                this.count = 0;
                return res;
            }
        }
        this.count = this.count - smsCount;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmsBalanceEntity that = (SmsBalanceEntity) o;
        return count == that.count &&
                Double.compare(that.price, price) == 0 &&
                Objects.equals(orgId, that.orgId) &&
                Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, price, orgId, storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("count", count)
                .add("price", price)
                .add("orgId", orgId)
                .add("storeId", storeId)
                .add("type", type)
                .toString();
    }
}
