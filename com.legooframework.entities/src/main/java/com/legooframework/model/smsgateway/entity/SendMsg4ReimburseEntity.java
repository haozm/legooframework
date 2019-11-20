package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.commons.entity.SendChannel;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.joda.time.LocalDateTime;

import java.sql.ResultSet;

public class SendMsg4ReimburseEntity extends BaseEntity<String> {

    private final boolean freeSend = false;
    private final SendChannel sendChannel = SendChannel.SMS;
    private final int smsCount;
    private final String sendBatchNo;
    private boolean reimburseState;
    private LocalDateTime reimburseDate;
//    reimburse_state       TINYINT UNSIGNED NOT NULL DEFAULT 0,
//    reimburse_state_date  DATETIME         NULL,

    SendMsg4ReimburseEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.smsCount = ResultSetUtil.getObject(res, "smsCount", Integer.class);
            this.sendBatchNo = ResultSetUtil.getString(res, "sendBatchNo");
            this.reimburseState = ResultSetUtil.getBooleanByInt(res, "reimburseState");
            if (this.reimburseState) {
                this.reimburseDate = LocalDateTime.fromDateFields(res.getTimestamp("reimburseDate"));
            } else {
                this.reimburseDate = null;
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Restore SendMsg4ReimburseEntity has SQLException", e);
        }
    }

    public int getSmsCount() {
        return smsCount;
    }

    public String getSendBatchNo() {
        return sendBatchNo;
    }

    public boolean isReimburse() {
        return reimburseState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SendMsg4ReimburseEntity that = (SendMsg4ReimburseEntity) o;
        return freeSend == that.freeSend &&
                smsCount == that.smsCount &&
                reimburseState == that.reimburseState &&
                sendChannel == that.sendChannel &&
                Objects.equal(sendBatchNo, that.sendBatchNo) &&
                Objects.equal(reimburseDate, that.reimburseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), freeSend, sendChannel, smsCount, sendBatchNo, reimburseState,
                reimburseDate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("freeSend", freeSend)
                .add("communicationChannel", sendChannel)
                .add("smsCount", smsCount)
                .add("sendBatchNo", sendBatchNo)
                .add("reimburseState", reimburseState)
                .add("reimburseDate", reimburseDate)
                .toString();
    }
}
