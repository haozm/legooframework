package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.legooframework.model.commons.entity.SendChannel;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SendMsg4ReimburseEntity extends BaseEntity<String> {

    private final boolean freeSend = false;
    private final SendChannel sendChannel = SendChannel.SMS;
    private final int smsCount;
    private final Integer companyId, storeId;

    private final String reimburseBatchNo;
    private boolean reimburseState;// 0 1 未开始 完成
    private LocalDateTime reimburseDate;

    SendMsg4ReimburseEntity(String id, ResultSet res) {
        super(id);
        try {
            this.smsCount = res.getInt("sms_count");
            this.companyId = res.getInt("company_id");
            this.storeId = res.getInt("store_id");
            this.reimburseState = res.getInt("reimburse_state") == 1;
            if (this.reimburseState) {
                this.reimburseBatchNo = ResultSetUtil.getString(res, "reimburse_batchno");
                this.reimburseDate = LocalDateTime.fromDateFields(res.getTimestamp("reimburse_date"));
            } else {
                this.reimburseDate = null;
                this.reimburseBatchNo = null;
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Restore SendMsg4ReimburseEntity has SQLException", e);
        }
    }

    public int getSmsCount() {
        return smsCount;
    }

    public boolean isReimbursed() {
        return reimburseState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendMsg4ReimburseEntity that = (SendMsg4ReimburseEntity) o;
        return freeSend == that.freeSend &&
                smsCount == that.smsCount &&
                reimburseState == that.reimburseState &&
                sendChannel == that.sendChannel &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(reimburseBatchNo, that.reimburseBatchNo) &&
                Objects.equal(reimburseDate, that.reimburseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), freeSend, sendChannel, smsCount, reimburseBatchNo, reimburseState,
                reimburseDate, companyId, storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("freeSend", freeSend)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("sendChannel", sendChannel)
                .add("smsCount", smsCount)
                .add("reimburseBatchNo", reimburseBatchNo)
                .add("reimburseState", reimburseState)
                .add("reimburseDate", reimburseDate)
                .toString();
    }
}
