package com.legooframework.model.smsresult.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SMSResultEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final SMSEntity sendSms;
    private final SMSChannel smsChannel;
    private String account;
    private final Long smsExt;
    //  提交返回
    private String sendMsgId;
    private SendState sendState;
    private LocalDateTime sendDate;
    private String sendRemark;

    // 其他参数
    private FinalState finalState;
    private String finalDesc;
    private LocalDateTime finalDate;

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, getId());
        ps.setObject(2, getCompanyId());
        ps.setObject(3, getStoreId());
        ps.setObject(4, getSmsChannel().getChannel());
        ps.setObject(5, sendState.getState());
        ps.setObject(6, getMobile());
        ps.setObject(7, getSendSms().getSmsNum());
        ps.setObject(8, getSendSms().getWordCount());
        ps.setObject(9, getSendSms().getContent());
        ps.setObject(10, getSmsExt());
        ps.setObject(11, getCompanyId());
    }

    public SMSResultEntity(Integer companyId, Integer storeId, SMSEntity sendSms, int smsChannel, long smsExt) {
        super(sendSms.getSmsId(), companyId.longValue(), -1L);
        this.companyId = companyId;
        this.storeId = storeId;
        this.smsExt = smsExt;
        this.sendSms = sendSms;
        this.finalState = FinalState.WAITING;
        this.smsChannel = SMSChannel.paras(smsChannel);
        this.sendState = SendState.WAITING;
    }

    SMSResultEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.smsExt = ResultSetUtil.getObject(res, "smsExt", Long.class);
            this.account = ResultSetUtil.getOptString(res, "sms_account", null);
            this.sendSms = SMSEntity.create4Sending(id, ResultSetUtil.getString(res, "smsContext"),
                    ResultSetUtil.getString(res, "phoneNo"), res.getInt("wordCount"), res.getInt("smsCount"));
            this.smsChannel = SMSChannel.paras(res.getInt("smsChannle"));

            this.sendState = SendState.paras(res.getInt("sendState"));
            this.sendMsgId = res.getString("sendMsgId");
            this.sendDate = res.getTimestamp("sendDate") == null ? null : LocalDateTime.fromDateFields(res.getTimestamp("sendDate"));
            this.sendRemark = ResultSetUtil.getOptString(res, "sendRemark", null);

            this.finalState = FinalState.paras(res.getInt("finalState"));
            this.finalDate = res.getTimestamp("finalDate") == null ? null : LocalDateTime.fromDateFields(res.getTimestamp("finalDate"));
            this.finalDesc = ResultSetUtil.getOptString(res, "finalDesc", null);

        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSResultEntity has SQLException", e);
        }
    }

    private Long getSmsExt() {
        return smsExt;
    }

    private Integer getCompanyId() {
        return companyId == null ? -1 : companyId;
    }

    private Integer getStoreId() {
        return storeId == null ? -1 : storeId;
    }

    private SMSEntity getSendSms() {
        return sendSms;
    }

    private SMSChannel getSmsChannel() {
        return smsChannel;
    }

    private String getMobile() {
        return sendSms.getPhoneNo();
    }

    public boolean hasResult() {
        return FinalState.DELIVRD == this.finalState || FinalState.UNDELIV == this.finalState;
    }

    public String toFinalState() {
        if (SendState.ERROR == this.sendState) {
            return String.format("%s|%s|9|%s|%s", getMobile(), getId(), this.sendDate == null ?
                            LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss") : this.sendDate.toString("yyyy-MM-dd HH:mm:ss"),
                    this.sendRemark == null ? "error:SENDERROR" : this.sendRemark);
        }
        return String.format("%s|%s|%s|%s|%s", getMobile(), getId(), this.finalState.getState(),
                FinalState.WAITING == this.finalState ? LocalDateTime.now().toString("yyyy-MM-dd HH:mm:ss")
                        : this.finalDate.toString("yyyy-MM-dd HH:mm:ss"),
                FinalState.WAITING == this.finalState ? "NONE" : this.finalState);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("companyId", "storeId", "smsChannel", "sendStatus", "sendSms");
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("smsExt", smsExt);
        params.put("smsChannel", smsChannel.getChannel());
        params.put("sendState", sendState.getState());
        params.put("phoneNo", sendSms.getPhoneNo());
        params.put("smsCount", sendSms.getSmsNum());
        params.put("wordCount", sendSms.getWordCount());
        params.put("smsContext", sendSms.getContent());
        params.put("finalState", finalState.getState());
        return params;
    }

    public Map<String, Object> toView4SyncState(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("smsExt", getSmsExt());
        params.put("smsChannle", getSmsChannel().getChannel());
        params.put("phoneNo", getMobile());
        params.put("sendDate", start == null ? sendDate : start.toDate());
        params.put("endDate", end == null ? LocalDateTime.now().toDate() : end);
        params.put("account", this.account);
        return params;
    }


}
