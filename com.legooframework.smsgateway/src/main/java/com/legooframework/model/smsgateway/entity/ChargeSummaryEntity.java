package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.membercare.entity.BusinessType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class ChargeSummaryEntity extends BaseEntity<String> {

    private final Integer companyId, storeId;
    private final long smsQuantity, wxQuantity;
    private final String smsBatchNo, smsContext, storeName, companyName;
    private final boolean freeSend;
    private final SendMode sendMode;
    private final BusinessType businessType;
    private boolean finishSend;

    private ChargeSummaryEntity(StoEntity store, BusinessType businessType, boolean freeSend, String smsBatchNo,
                                long smsQuantity, long wxQuantity, long cteator, SendMode sendMode, String smsContext) {
        super(UUID.randomUUID().toString().toUpperCase(), store.getCompanyId().longValue(), cteator);
        this.companyId = store.getCompanyId();
        this.companyName = "公司";
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.smsQuantity = smsQuantity;
        this.freeSend = freeSend;
        this.smsBatchNo = smsBatchNo;
        this.sendMode = sendMode;
        this.wxQuantity = wxQuantity;
        this.businessType = businessType;
        this.smsContext = smsContext;
        this.finishSend = false;
    }

    private ChargeSummaryEntity(StoEntity store, BusinessType businessType, String smsBatchNo,
                                long smsQuantity, long wxQuantity, long cteator, SendMode sendMode, String smsContext) {
        super(UUID.randomUUID().toString(), store.getCompanyId().longValue(), cteator);
        this.companyId = store.getCompanyId();
        this.companyName = "公司";
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.smsQuantity = smsQuantity;
        this.freeSend = true;
        this.smsBatchNo = smsBatchNo;
        this.sendMode = sendMode;
        this.wxQuantity = wxQuantity;
        this.businessType = businessType;
        this.smsContext = smsContext;
        this.finishSend = false;
    }

    static ChargeSummaryEntity createInstance(StoEntity store, SMSSendRuleEntity businessRule, BusinessType businessType,
                                              String smsBatchNo, boolean isAuto, long smsQuantity, long wxQuantity, String smsContext) {
        Preconditions.checkArgument(!(smsQuantity <= 0 && wxQuantity <= 0), "本次发送短信或者微信数量不可都为0");
        LoginContext user = isAuto ? null : LoginContextHolder.get();
        SendMode sendMode = (smsQuantity == 1L || wxQuantity == 1L) ? SendMode.ManualSingle : SendMode.ManualBatch;
        if (businessRule == null)
            return new ChargeSummaryEntity(store, businessType, smsBatchNo, smsQuantity, wxQuantity, isAuto ? -1 : user.getLoginId(),
                    sendMode, smsContext);
        return new ChargeSummaryEntity(store, businessRule.getBusinessType(), businessRule.isFreeSend(), smsBatchNo,
                smsQuantity, wxQuantity, isAuto ? -1 : user.getLoginId(), sendMode, smsContext);
    }

    ChargeSummaryEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.smsQuantity = ResultSetUtil.getObject(res, "smsQuantity", Long.class);
            this.wxQuantity = ResultSetUtil.getObject(res, "wxQuantity", Long.class);
            this.freeSend = ResultSetUtil.getBooleanByInt(res, "freeSend");
            this.smsBatchNo = ResultSetUtil.getObject(res, "smsBatchNo", String.class);
            this.smsContext = ResultSetUtil.getOptString(res, "smsContext", null);
            this.storeName = ResultSetUtil.getOptString(res, "storeName", null);
            this.companyName = ResultSetUtil.getOptString(res, "companyName", null);
            this.businessType = BusinessType.parse(ResultSetUtil.getObject(res, "businessType", String.class));
            this.sendMode = SendMode.paras(ResultSetUtil.getObject(res, "sendMode", Integer.class));
            this.finishSend = ResultSetUtil.getBooleanByInt(res, "finishSend");
        } catch (SQLException e) {
            throw new RuntimeException("Restore ChargeSummaryEntity has SQLException", e);
        }
    }

    public String getSmsBatchNo() {
        return smsBatchNo;
    }

    boolean isFinishSend() {
        return finishSend;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("smsBatchNo", smsBatchNo);
        params.put("smsQuantity", smsQuantity < 0 ? 0 : smsQuantity);
        params.put("freeSend", freeSend ? 1 : 0);
        params.put("sendMode", sendMode.getMode());
        params.put("businessType", businessType.toString());
        params.put("smsContext", smsContext);
        params.put("companyName", companyName);
        params.put("storeName", storeName);
        params.put("finishSend", finishSend ? 1 : 0);
        params.put("wxQuantity", wxQuantity < 0 ? 0 : wxQuantity);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChargeSummaryEntity)) return false;
        ChargeSummaryEntity that = (ChargeSummaryEntity) o;
        return smsQuantity == that.smsQuantity &&
                wxQuantity == that.wxQuantity &&
                finishSend == that.finishSend &&
                freeSend == that.freeSend &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(smsContext, that.smsContext) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(smsBatchNo, that.smsBatchNo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, smsQuantity, wxQuantity, finishSend, freeSend, smsBatchNo,
                smsContext);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("smsQuantity", smsQuantity)
                .add("wxQuantity", wxQuantity)
                .add("freeSend", freeSend)
                .add("smsBatchNo", smsBatchNo)
                .add("sendMode", sendMode)
                .add("finishSend", finishSend)
                .add("businessType", businessType)
                .add("smsContext", smsContext)
                .add("create", getCreator())
                .toString();
    }
}
