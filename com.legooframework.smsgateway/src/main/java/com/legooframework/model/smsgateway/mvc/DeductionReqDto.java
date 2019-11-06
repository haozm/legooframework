package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsgateway.entity.SendMode;

import java.util.List;

public class DeductionReqDto {

    private final BusinessType businessType;
    private final List<SMSEntity> smses;
    private final StoEntity store;
    private final String smsContext;
    private final SendMode sendMode;

    DeductionReqDto(StoEntity store, BusinessType businessType, List<SMSEntity> smses, String smsContext,
                    SendMode sendMode) {
        Preconditions.checkNotNull(businessType, "业务类型businessType 非法为空值");
        this.businessType = businessType;
        this.smses = smses;
        this.store = store;
        this.smsContext = smsContext;
        this.sendMode = sendMode;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public List<SMSEntity> getSmses() {
        return smses;
    }

    public StoEntity getStore() {
        return store;
    }

    public String getSmsContext() {
        return smsContext;
    }

    public SendMode getSendMode() {
        return sendMode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("businessType", businessType)
                .add("store", store.getName())
                .add("smses", smses.size())
                .add("smsContext", smsContext)
                .toString();
    }
}
