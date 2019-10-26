package com.legooframework.model.insurance.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.dict.entity.KvDictEntity;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class InsuranceInfoEntity extends BaseEntity<Integer> implements BatchSetter {

    private Integer insuranceId;
    private String insuranceType, insuranceName;
    private BigDecimal insuranceAmount;
    private boolean primary;

    public InsuranceInfoEntity(KvDictEntity insuranceType, String insuranceAmount, String primary) {
        super(UUID.randomUUID().toString().hashCode());
        Preconditions.checkArgument(StringUtils.equals("INSURANCE", insuranceType.getType()));
        this.insuranceType = insuranceType.getValue();
        this.insuranceAmount = new BigDecimal(insuranceAmount);
        this.primary = StringUtils.equals("true", primary);
        this.insuranceName = insuranceType.getName();
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // insurance_id, insurance_type, insurance_amount
        ps.setObject(1, this.insuranceId);
        ps.setObject(2, this.insuranceType);
        ps.setObject(3, this.insuranceAmount);
    }

    void setInsuranceId(InsurancePolicyEntity policyEntity) {
        this.insuranceId = policyEntity.getId();
    }

    public boolean isPrimary() {
        return primary;
    }

    InsuranceInfoEntity(Long id, ResultSet res) {
        super(id.intValue());
        try {
            this.insuranceId = ResultSetUtil.getObject(res, "insuranceId", Long.class).intValue();
            this.insuranceType = ResultSetUtil.getString(res, "insuranceType");
            this.insuranceAmount = ResultSetUtil.getObject(res, "insuranceAmount", BigDecimal.class);
            this.insuranceName = ResultSetUtil.getString(res, "insuranceName");
            this.primary = ResultSetUtil.getBooleanByInt(res, "primary");
        } catch (SQLException e) {
            throw new RuntimeException("Restore InsuranceInfoEntity has SQLException", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InsuranceInfoEntity)) return false;
        InsuranceInfoEntity that = (InsuranceInfoEntity) o;
        return primary == that.primary &&
                Objects.equal(getId(), that.getId()) &&
                Objects.equal(insuranceId, that.insuranceId) &&
                Objects.equal(insuranceType, that.insuranceType) &&
                Objects.equal(insuranceAmount, that.insuranceAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), insuranceId, insuranceType, insuranceAmount, primary);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("insuranceId", insuranceId)
                .add("insuranceType", insuranceType)
                .add("insuranceAmount", insuranceAmount.toString())
                .add("primary", primary)
                .add("insuranceName", insuranceName)
                .toString();
    }
}
