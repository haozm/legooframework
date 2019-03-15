package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SMSTransportLogEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final SMSEntity sms;
    private final String smsBatchNo;
    private final SMSChannel smsChannel;
    private final String businessType;
    private final boolean freeSend;
    private SendStatus sendStatus;
    // 读取状态
    private String resCode;
    private String remarks;

    public SMSTransportLogEntity(CrmStoreEntity store, SMSEntity sms, String smsBatchNo, SMSSendRuleEntity sendRule) {
        super(sms.getSmsId(), store.getCompanyId().longValue(), -1L);
        this.sms = sms;
        this.companyId = store.getCompanyId();
        this.storeId = store.getId();
        this.sendStatus = SendStatus.Transport;
        this.smsBatchNo = smsBatchNo;
        this.smsChannel = sendRule.getSmsChannel();
        this.freeSend = sendRule.isFreeSend();
        this.businessType = sendRule.getBusinessType();
    }

    SMSTransportLogEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.smsBatchNo = ResultSetUtil.getString(res, "smsBatchNo");
            this.businessType = ResultSetUtil.getString(res, "businessType");
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.sendStatus = SendStatus.paras(ResultSetUtil.getObject(res, "sendStatus", Integer.class));
            this.smsChannel = SMSChannel.paras(ResultSetUtil.getObject(res, "smsChannel", Integer.class));
            this.freeSend = ResultSetUtil.getBooleanByInt(res, "freeSend");
            this.resCode = ResultSetUtil.getOptString(res, "resCode", null);
            this.remarks = ResultSetUtil.getOptString(res, "remarks", null);
            this.sms = new SMSEntity(ResultSetUtil.getString(res, "smsContext"),
                    ResultSetUtil.getString(res, "phoneNum"), ResultSetUtil.getString(res, "id"),
                    ResultSetUtil.getObject(res, "wordCount", Integer.class),
                    ResultSetUtil.getObject(res, "smsCount", Integer.class),
                    ResultSetUtil.getOptObject(res, "memberId", Integer.class).orElse(-1),
                    ResultSetUtil.getOptString(res, "memberName", null));
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSTransportLogEntity has SQLException", e);
        }
    }

    public SMSEntity getSms() {
        return sms;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    void set4Sending() {
        this.sendStatus = SendStatus.Sending;
    }

    public void errorByPhoneNo() {
        this.sendStatus = SendStatus.Error;
        this.resCode = "7000";
        this.remarks = "非法的电话号码或者号码为空号";
    }

    public void errorUnknow(Exception e) {
        this.sendStatus = SendStatus.Error;
        this.resCode = "7999";
        this.remarks = e.getMessage();
    }

    public void errorByBlackList() {
        this.sendStatus = SendStatus.Error;
        this.resCode = "7100";
        this.remarks = "号码设置无效或者回复TD退订";
    }

    public boolean isError() {
        return SendStatus.Error == this.sendStatus;
    }

    SendStatus getSendStatus() {
        return sendStatus;
    }

    String getResCode() {
        return resCode;
    }

    String getRemarks() {
        return remarks;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        // id, company_id, 3store_id, 4member_id,5 sms_batchno, 6 phone_no,7 sms_count,
        // 8 word_count, member_name, sms_context, tenant_id, creator
        ps.setObject(1, sms.getSmsId());
        ps.setObject(2, this.companyId);
        ps.setObject(3, this.storeId);
        ps.setObject(4, sms.getMemberId());
        ps.setObject(5, this.smsBatchNo);
        ps.setObject(6, sms.getPhoneNo());
        ps.setObject(7, sms.getSmsNum());
        ps.setObject(8, sms.getWordCount());
        ps.setObject(9, sms.getMemberName());
        ps.setObject(10, sms.getContent());
        ps.setObject(11, this.companyId);
        ps.setObject(12, this.getCreator());
        ps.setObject(13, this.freeSend ? 1 : 0);
        ps.setObject(14, this.smsChannel.getChannel());
        ps.setObject(15, this.businessType);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("sms", "batchNo", "smsChannel", "sendStatus");
        params.put("smsBatchNo", smsBatchNo);
        params.put("sendStatus", sendStatus.getStatus());
        params.put("smsCount", sms.getSmsNum());
        params.put("wordCount", sms.getWordCount());
        params.put("smsContext", sms.getContent());
        params.put("phoneNum", sms.getPhoneNo());
        params.put("smsChannel", smsChannel);
        params.put("freeSend", freeSend ? 1 : 0);
        params.put("businessType", businessType);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("sms", sms)
                .add("smsBatchNo", smsBatchNo)
                .add("smsChannel", smsChannel)
                .add("businessType", businessType)
                .add("freeSend", freeSend)
                .add("sendStatus", sendStatus)
                .toString();
    }
}
