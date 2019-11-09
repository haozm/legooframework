package com.legooframework.model.smsresult.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsgateway.entity.SendStatus;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class SMSSendAndReceiveEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final SMSEntity sendSms;
    private final SMSChannel smsChannel;
    private final SendStatus sendStatus;
    private final Long smsExt;
    //  提交返回
    private String sendMsgId;
    private Date sendDate;
    private String remarks;
    // 其他参数
    private FinalState finalState;
    private String finalStateDesc;
    private Date finalStateDate;

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getId());
        ps.setObject(2, getCompanyId());
        ps.setObject(3, getStoreId());
        ps.setObject(4, getSmsChannel().getChannel());
        ps.setObject(5, getSendStatus().getStatus());
        ps.setObject(6, getMobile());
        ps.setObject(7, getSendSms().getSmsNum());
        ps.setObject(8, getSendSms().getWordCount());
        ps.setObject(9, getSendSms().getContent());
        ps.setObject(10, getSmsExt());
        ps.setObject(11, getCompanyId());
        ps.setObject(12, getFinalState().getState());
    }

    SMSSendAndReceiveEntity(Integer companyId, Integer storeId, SMSEntity sendSms, int smsChannel, int sendStatus, long smsExt) {
        super(sendSms.getSmsId(), companyId.longValue(), -1L);
        this.companyId = companyId;
        this.storeId = storeId;
        this.smsExt = smsExt;
        this.sendSms = sendSms;
        this.finalState = FinalState.WAITING;
        this.smsChannel = SMSChannel.paras(smsChannel);
        SendStatus _sendStatus = SendStatus.paras(sendStatus);
        Preconditions.checkState(SendStatus.SendedGateWay == _sendStatus, "非法的短信状态...");
        this.sendStatus = SendStatus.SendedGateWay;
        this.finalStateDesc = null;
        this.finalStateDate = null;
    }

    SMSSendAndReceiveEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.smsExt = ResultSetUtil.getObject(res, "smsExt", Long.class);
            this.sendSms = SMSEntity.create4Sending(id, ResultSetUtil.getString(res, "smsContext"),
                    ResultSetUtil.getString(res, "phoneNo"), res.getInt("wordCount"), res.getInt("smsCount"));
            this.smsChannel = SMSChannel.paras(res.getInt("smsChannle"));
            this.sendStatus = SendStatus.paras(res.getInt("sendStatus"));
            this.sendMsgId = res.getString("sendMsgId");
            this.sendDate = res.getTimestamp("sendDate");
            this.remarks = ResultSetUtil.getOptString(res, "remarks", null);
            this.finalState = FinalState.paras(res.getInt("finalState"));
            this.finalStateDate = res.getTimestamp("finalStateDate");
            this.finalStateDesc = ResultSetUtil.getOptString(res, "finalStateDesc", null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSSendEntity has SQLException", e);
        }
    }

    FinalState getFinalState() {
        return finalState;
    }

    Long getSmsExt() {
        return smsExt;
    }

    SendStatus getSendStatus() {
        return sendStatus;
    }

    String getRemarks() {
        return remarks;
    }

    Integer getCompanyId() {
        return companyId == null ? -1 : companyId;
    }

    Integer getStoreId() {
        return storeId == null ? -1 : storeId;
    }

    SMSEntity getSendSms() {
        return sendSms;
    }

    public SMSChannel getSmsChannel() {
        return smsChannel;
    }

    public String getMobile() {
        return sendSms.getPhoneNo();
    }

    public boolean hasResult() {
        return FinalState.SENDEDERROR == this.finalState || FinalState.DELIVRD == this.finalState ||
                FinalState.UNDELIV == this.finalState;
    }

    public String toFinalState() {
        if (FinalState.SENDEDERROR == getFinalState()) {
            return String.format("%s|4|2|%s|%s", getId(), DateFormatUtils.format(this.sendDate, "yyyy-MM-dd HH:mm:ss"),
                    this.remarks == null ? "error:SENDERROR" : this.remarks);
        }
        if (FinalState.DELIVRD == getFinalState()) {
            return String.format("%s|3|1|%s|DELIVRD", getId(), DateFormatUtils.format(this.finalStateDate, "yyyy-MM-dd HH:mm:ss"));
        }
        return String.format("%s|4|2|%s|%s", getId(), DateFormatUtils.format(this.finalStateDate, "yyyy-MM-dd HH:mm:ss"),
                finalStateDesc == null ? "ERROR" : finalStateDesc);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("companyId", "storeId", "smsChannel", "sendStatus", "sendSms");
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("smsExt", smsExt);
        params.put("smsChannel", smsChannel.getChannel());
        params.put("sendStatus", sendStatus.getStatus());
        params.put("phoneNo", sendSms.getPhoneNo());
        params.put("smsCount", sendSms.getSmsNum());
        params.put("wordCount", sendSms.getWordCount());
        params.put("smsContext", sendSms.getContent());
        params.put("finalState", finalState.getState());
        return params;
    }

    void set4Sending() {
        this.finalState = FinalState.SENDING;
    }

}
