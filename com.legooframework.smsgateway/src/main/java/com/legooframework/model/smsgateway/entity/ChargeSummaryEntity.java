package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class ChargeSummaryEntity extends BaseEntity<String> {

    private final Integer companyId, storeId;
    private final long smsQuantity;
    private final String businessRuleId, smsBatchNo, businessType, smsContext, operatorName, storeName, companyName;
    private final SendMode sendMode;
    private boolean finishSend;
    private List<SendStatus> sendStatuses;

    private ChargeSummaryEntity(CrmStoreEntity store, SMSSendRuleEntity businessRule, String smsBatchNo,
                                long smsQuantity, long cteater, SendMode sendMode, String smsContext, String operatorName) {
        super(UUID.randomUUID().toString().toUpperCase(), store.getCompanyId().longValue(), cteater);
        Preconditions.checkState(smsQuantity > 0, "短信数量不可以为负数或者为0....");
        this.companyId = store.getCompanyId();
        this.companyName = store.getCompanyName();
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.smsQuantity = smsQuantity;
        this.businessRuleId = businessRule.getId();
        this.smsBatchNo = smsBatchNo;
        this.sendMode = sendMode;
        this.operatorName = operatorName;
        this.businessType = businessRule.getBusinessType();
        this.smsContext = smsContext;
        this.finishSend = false;
    }

    static ChargeSummaryEntity manual(CrmStoreEntity store, SMSSendRuleEntity businessRule, String smsBatchNo,
                                      long smsQuantity, String smsContext, LoginContext user) {
        SendMode sendMode = smsQuantity == 1 ? SendMode.ManualSingle : SendMode.ManualBatch;
        return new ChargeSummaryEntity(store, businessRule, smsBatchNo, smsQuantity, user.getLoginId(),
                sendMode, smsContext, user.getLoginName());
    }

    static ChargeSummaryEntity autoJob(CrmStoreEntity store, SMSSendRuleEntity businessRule, String smsBatchNo,
                                       long smsQuantity, String smsContext) {
        return new ChargeSummaryEntity(store, businessRule, smsBatchNo, smsQuantity, -1L, SendMode.AutoJob,
                smsContext, null);
    }

    ChargeSummaryEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.smsQuantity = ResultSetUtil.getObject(res, "smsQuantity", Long.class);
            this.businessRuleId = ResultSetUtil.getObject(res, "businessRuleId", String.class);
            this.smsBatchNo = ResultSetUtil.getObject(res, "smsBatchNo", String.class);
            this.smsContext = ResultSetUtil.getObject(res, "smsContext", String.class);
            this.storeName = ResultSetUtil.getOptString(res, "storeName", null);
            this.companyName = ResultSetUtil.getOptString(res, "companyName", null);
            this.operatorName = ResultSetUtil.getOptString(res, "operatorName", null);
            this.businessType = ResultSetUtil.getObject(res, "businessType", String.class);
            this.sendMode = SendMode.paras(ResultSetUtil.getObject(res, "sendMode", Integer.class));
            this.finishSend = ResultSetUtil.getBooleanByInt(res, "finishSend");
            String _statuses = ResultSetUtil.getString(res, "detailStatus");
            this.sendStatuses = Lists.newArrayList();
            Stream.of(StringUtils.split(_statuses, ',')).mapToInt(Integer::valueOf)
                    .forEach(val -> this.sendStatuses.add(SendStatus.paras(val)));
        } catch (SQLException e) {
            throw new RuntimeException("Restore ChargeSummaryEntity has SQLException", e);
        }
    }

    String getSmsBatchNo() {
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
        params.put("smsQuantity", smsQuantity);
        params.put("businessRuleId", businessRuleId);
        params.put("sendMode", sendMode.getMode());
        params.put("businessType", businessType);
        params.put("smsContext", smsContext);
        params.put("companyName", companyName);
        params.put("storeName", storeName);
        params.put("operatorName", operatorName);
        params.put("finishSend", finishSend ? 1 : 0);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChargeSummaryEntity)) return false;
        ChargeSummaryEntity that = (ChargeSummaryEntity) o;
        return smsQuantity == that.smsQuantity &&
                finishSend == that.finishSend &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(smsContext, that.smsContext) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(businessRuleId, that.businessRuleId) &&
                Objects.equal(smsBatchNo, that.smsBatchNo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, smsQuantity, finishSend, businessRuleId, smsBatchNo,
                smsContext);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("smsQuantity", smsQuantity)
                .add("businessRuleId", businessRuleId)
                .add("smsBatchNo", smsBatchNo)
                .add("sendMode", sendMode)
                .add("finishSend", finishSend)
                .add("businessType", businessType)
                .add("smsContext", smsContext)
                .add("create", getCreator())
                .add("operatorName", operatorName)
                .toString();
    }
}
