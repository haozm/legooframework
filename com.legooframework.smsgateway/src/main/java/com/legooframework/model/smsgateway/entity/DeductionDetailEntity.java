package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * 短信扣费明细
 */
public class DeductionDetailEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final String smsBatchNo, balanceId;
    private final long balanceNum;
    private long deductionNum, reimburseNum;
    private final RechargeScope rechargeScope;

    DeductionDetailEntity(MsgTransportBatchEntity transportBatch, RechargeBalanceEntity balance) {
        super(UUID.randomUUID().toString().toUpperCase(), transportBatch.getCompanyId().longValue(), -1L);
        this.smsBatchNo = transportBatch.getBatchNo();
        this.companyId = transportBatch.getCompanyId();
        this.storeId = transportBatch.getStoreId();
        this.balanceId = balance.getId();
        this.balanceNum = balance.getBalance();
        this.deductionNum = 0;
        this.reimburseNum = 0;
        this.rechargeScope = balance.getRechargeScope();
    }

    void setDeductionNum(long deductionNum) {
        this.deductionNum = deductionNum;
    }

    String getSmsBatchNo() {
        return smsBatchNo;
    }

    long getReimburseNum() {
        return reimburseNum;
    }

    public String getBalanceId() {
        return balanceId;
    }

    public long getWriteOffNum() {
        Preconditions.checkState(deductionNum - reimburseNum >= 0);
        return deductionNum - reimburseNum;
    }

    RechargeScope getRechargeScope() {
        return rechargeScope;
    }

    public void reimburse(long smsNum) {
        Preconditions.checkArgument(reimburseNum + smsNum <= deductionNum);
        reimburseNum += smsNum;
    }

    DeductionDetailEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.smsBatchNo = ResultSetUtil.getString(res, "smsBatchNo");
            this.balanceId = ResultSetUtil.getString(res, "blanceId");
            this.balanceNum = ResultSetUtil.getObject(res, "balanceNum", Long.class);
            this.deductionNum = ResultSetUtil.getObject(res, "deductionNum", Long.class);
            this.reimburseNum = ResultSetUtil.getObject(res, "reimburseNum", Long.class);
            this.rechargeScope = RechargeScope.paras(ResultSetUtil.getObject(res, "rechargeScope", Integer.class));
        } catch (SQLException e) {
            throw new RuntimeException("Restore ChargeDetailEntity has SQLException", e);
        }
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//id, company_id, store_id, sms_batchno, blance_id, blance_num, deduction_num, tenant_id
        ps.setObject(1, getId());
        ps.setObject(2, companyId);
        ps.setObject(3, storeId);
        ps.setObject(4, smsBatchNo);
        ps.setObject(5, balanceId);
        ps.setObject(6, balanceNum);
        ps.setObject(7, deductionNum);
        ps.setObject(8, getTenantId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeductionDetailEntity)) return false;
        DeductionDetailEntity that = (DeductionDetailEntity) o;
        return balanceNum == that.balanceNum &&
                deductionNum == that.deductionNum &&
                reimburseNum == that.reimburseNum &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(smsBatchNo, that.smsBatchNo) &&
                Objects.equal(balanceId, that.balanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, smsBatchNo, balanceId, balanceNum, deductionNum, reimburseNum);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("smsBatchNo", smsBatchNo)
                .add("balanceId", balanceId)
                .add("balanceNum", balanceNum)
                .add("deductionNum", deductionNum)
                .add("reimburseNum", reimburseNum)
                .toString();
    }
}
