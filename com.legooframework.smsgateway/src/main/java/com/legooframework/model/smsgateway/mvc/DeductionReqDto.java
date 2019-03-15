package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.MoreObjects;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.smsgateway.entity.SMSEntity;

import java.util.List;

public class DeductionReqDto {

    private final KvDictEntity businessType;
    private final List<SMSEntity> smses;
    private final CrmStoreEntity store;
    private final String smsContext;

    DeductionReqDto(CrmStoreEntity store, KvDictEntity businessType, List<SMSEntity> smses, String smsContext) {
        this.businessType = businessType;
        this.smses = smses;
        this.store = store;
        this.smsContext = smsContext;
    }

    public KvDictEntity getBusinessType() {
        return businessType;
    }

    public List<SMSEntity> getSmses() {
        return smses;
    }

    public CrmStoreEntity getStore() {
        return store;
    }

    public String getSmsContext() {
        return smsContext;
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
