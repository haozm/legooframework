package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;

public class SendMsg4DeductionEntity extends BaseEntity<String> {

    private int smsCount;

    SendMsg4DeductionEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.smsCount = res.getInt("sms_count");
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Restore SendMsg4DeductionEntity has SQLException", e);
        }
    }


    int getSmsCount() {
        return smsCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("smsCount", smsCount)
                .toString();
    }
}
