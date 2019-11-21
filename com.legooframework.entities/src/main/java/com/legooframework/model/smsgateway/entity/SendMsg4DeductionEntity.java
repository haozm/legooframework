package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;

public class SendMsg4DeductionEntity extends BaseEntity<String> {

    private int smsCount;
    private SendStatus sendStatus;
    private String remarks;

    SendMsg4DeductionEntity(String id, ResultSet res) {
        super(id);
        try {
            this.smsCount = res.getInt("sms_count");
            this.sendStatus = SendStatus.paras(res.getInt("send_status"));
            this.remarks = res.getString("remarks");
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Restore SendMsg4DeductionEntity has SQLException", e);
        }
    }

    SendStatus getSendStatus() {
        return sendStatus;
    }

    String getRemarks() {
        return remarks;
    }

    public void deductionOK() {
        this.sendStatus = SendStatus.SMS4Storage;
    }

    public void deductionFail() {
        this.sendStatus = SendStatus.SMS4SendError;
        this.remarks = "计费失败...";
    }

    int getSmsCount() {
        return smsCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("smsCount", smsCount)
                .add("sendStatus", sendStatus)
                .add("remarks", remarks)
                .toString();
    }
}
