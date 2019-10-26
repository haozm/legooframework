package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.utils.CommonsUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RechargeDeductionDetailEntity extends BaseEntity<String> implements BatchSetter {

    private final String rechargeId, deductionId, recordId;
    private int finished = 1;

    RechargeDeductionDetailEntity(RechargeDetailEntity rechare,
                                  RechargeDetailEntity deduction, RechargeDetailEntity record, LoginContext user) {
        super(CommonsUtils.randomId(16).toUpperCase(), rechare.getTenantId(), user.getLoginId());
        this.rechargeId = rechare.getId();
        this.recordId = record.getId();
        this.deductionId = deduction.getId();
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        //id, recharge_id, deduction_id, record_id, creator
        ps.setObject(1, this.getId());
        ps.setObject(2, this.rechargeId);
        ps.setObject(3, this.deductionId);
        ps.setObject(4, this.recordId);
        ps.setObject(5, this.finished);
        ps.setObject(6, this.getCreator());
    }

    public String getRechargeId() {
        return rechargeId;
    }

    public String getDeductionId() {
        return deductionId;
    }

    public String getRecordId() {
        return recordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RechargeDeductionDetailEntity)) return false;
        RechargeDeductionDetailEntity that = (RechargeDeductionDetailEntity) o;
        return Objects.equal(rechargeId, that.rechargeId) &&
                Objects.equal(recordId, that.recordId) &&
                this.finished == that.finished &&
                Objects.equal(deductionId, that.deductionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rechargeId, deductionId, recordId, finished);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rechargeId", rechargeId)
                .add("deductionId", deductionId)
                .add("recordId", recordId)
                .add("finished", finished)
                .add("createTime", getCreateTime())
                .toString();
    }
}
