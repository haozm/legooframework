package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsprovider.entity.SMSChannel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SendMsg4InitEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final MsgEntity sms;
    private String sendBatchNo;
    private final SMSChannel smsChannel;
    private final BusinessType businessType;
    private final boolean freeSend;
    private SendStatus sendStatus;

    private SendMsg4InitEntity(Integer companyId, Integer storeId, MsgEntity sms, boolean freeSend) {
        super(sms.getSmsId(), companyId.longValue(), -1L);
        this.sms = sms;
        if (this.sms.isEnbaled()) {
            this.sendStatus = SendStatus.SMS4Inited;
        } else {
            this.sendStatus = SendStatus.SMS4InitError;
        }
        this.companyId = companyId;
        this.storeId = storeId;
        this.smsChannel = sms.getBusinessType().getSMSChannel();
        this.freeSend = freeSend;
        this.businessType = sms.getBusinessType();
    }

    void setSendBatchNo(String sendBatchNo) {
        if (Strings.isNullOrEmpty(this.sendBatchNo))
            this.sendBatchNo = sendBatchNo;
    }

    public static SendMsg4InitEntity createInstance(StoEntity store, MsgEntity sms, boolean freeSend) {
        return new SendMsg4InitEntity(store.getCompanyId(), store.getId(), sms, freeSend);
    }

    public static SendMsg4InitEntity createInstance(StoEntity store, MsgEntity sms) {
        return new SendMsg4InitEntity(store.getCompanyId(), store.getId(), sms, false);
    }

    public boolean isSMSMsg() {
        return sms.isSMSMsg();
    }

    public boolean isWxMsg() {
        return sms.isWxMsg();
    }

    public Integer getStoreId() {
        return storeId;
    }

    SendMsg4InitEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.sendBatchNo = ResultSetUtil.getString(res, "sendBatchNo");
            this.businessType = BusinessType.parse(ResultSetUtil.getString(res, "businessType"));
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.sendStatus = SendStatus.paras(ResultSetUtil.getObject(res, "sendStatus", Integer.class));
            this.smsChannel = SMSChannel.paras(ResultSetUtil.getObject(res, "smsChannel", Integer.class));
            this.freeSend = ResultSetUtil.getBooleanByInt(res, "freeSend");
            this.sms = MsgEntity.createInstance(res);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Restore SendMsg4InitEntity has SQLException", e);
        }
    }

    public String getPhoneNo() {
        return sms.getPhoneNo();
    }

    public MsgEntity getSms() {
        return sms;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public SMSChannel getSmsChannel() {
        return smsChannel;
    }

    public boolean isEnbaled() {
        return sms.isEnbaled();
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//  id, company_id, store_id, member_id, send_batchno, " +
//  "phone_no, sms_count, word_count, member_name, sms_context, tenant_id, creator, free_send, sms_channel, " +
//  "businsess_type, send_status, job_id, sms_enabled, communication_channel, weixin_id, device_id, remarks
        ps.setObject(1, sms.getSmsId());
        ps.setObject(2, this.companyId);
        ps.setObject(3, this.storeId);
        ps.setObject(4, sms.getMemberId());
        ps.setObject(5, this.sendBatchNo);
        ps.setObject(6, sms.getPhoneNo());
        ps.setObject(7, sms.getSmsNum());
        ps.setObject(8, sms.getWordCount());
        ps.setObject(9, sms.getMemberName());
        ps.setObject(10, sms.getContent());
        ps.setObject(11, this.companyId);
        ps.setObject(12, this.getCreator());
        ps.setObject(13, this.freeSend ? 1 : 0);
        ps.setObject(14, this.smsChannel.getChannel());
        ps.setObject(15, this.businessType.getType());
        ps.setObject(16, this.sms.getJobId());
        ps.setObject(17, this.sms.isEnbaled() ? 1 : 0);
        ps.setObject(18, sms.getSendChannel().getChannel());
        ps.setObject(19, sms.getWeixinId());
        ps.setObject(20, sms.getDeviceId());
        ps.setObject(21, sms.getRemark());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", sms.getSmsId())
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("sendBatchNo", sendBatchNo)
                .add("smsChannel", smsChannel)
                .add("businessType", businessType)
                .add("freeSend", freeSend)
                .add("sendStatus", sendStatus)
                .add("sms", sms)
                .toString();
    }
}
