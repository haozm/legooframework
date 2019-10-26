package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;

public class SendMsg4SendEntity extends SendMsg4InitEntity {

    private String sendResCode;
    private String remarks;
    private Date sendLocalDate;

    SendMsg4SendEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.sendResCode = ResultSetUtil.getOptString(res, "sendResCode", null);
            this.remarks = ResultSetUtil.getOptString(res, "remarks", null);
            this.sendLocalDate = res.getDate("sendLocalDate");
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Restore SMSSendEntity has SQLException", e);
        }
    }

    boolean read4SendWx() {
        return super.getSms().isEnbaled() && !super.getSms().isSMSMsg() &&
                StringUtils.isNotEmpty(super.getSms().getWeixinId()) &&
                StringUtils.isNotEmpty(super.getSms().getDeviceId());
    }

    String getSendResCode() {
        return sendResCode;
    }

    String getRemarks() {
        return remarks;
    }

    Date getSendLocalDate() {
        return sendLocalDate;
    }

    public boolean isError() {
        return SendStatus.SMS4SendError == getSendStatus();
    }

    public boolean isSending() {
        return SendStatus.SMS4Sending == getSendStatus();
    }

    void toSending() {
        setSendStatus(SendStatus.SMS4Sending);
    }

    public void errorByException(Exception e) {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        super.setSendStatus(SendStatus.SMS4SendError);
        this.sendResCode = "9999";
        String _msg = e.getMessage();
        this.remarks = _msg != null && _msg.length() > 512 ? _msg.substring(0, 511) : _msg;
    }

    public void error4Encode() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        super.setSendStatus(SendStatus.SMS4SendError);
        this.sendResCode = "7102";
        this.remarks = "编码为UNCODE码无效";
    }

    public void error4Init() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        super.setSendStatus(SendStatus.SMS4SendError);
        this.sendResCode = "7104";
        this.remarks = "短信生成异常";
    }

    public void errorByBlackList() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        super.setSendStatus(SendStatus.SMS4SendError);
        this.sendResCode = "7101";
        this.remarks = "号码设置无效或者回复TD退订";
    }

    public void errorBySending(String msg) {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        super.setSendStatus(SendStatus.SMS4SendError);
        this.sendResCode = "7103";
        this.remarks = Strings.isNullOrEmpty(msg) ? "发送信息失败" : msg;
    }

    public void errorByMobile() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        super.setSendStatus(SendStatus.SMS4SendError);
        this.sendResCode = "7100";
        this.remarks = "非法的电话或者号码无效";
    }

    public void finshedSend() {
        super.setSendStatus(SendStatus.SendedGateWay);
        this.sendLocalDate = DateTime.now().toDate();
    }

    public void finshedByWechat() {
        super.setSendStatus(SendStatus.SendedByWechat);
        this.sendLocalDate = DateTime.now().toDate();
    }


    public boolean isFinshed() {
        return SendStatus.SendedGateWay == super.getSendStatus();
    }

    public Map<String, Object> toSendPayload(boolean encoding) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getSms().getSmsId());
        params.put("content", encoding ? getSms().getEncodeCtx() : getSms().getContent());
        params.put("encoding", encoding);
        params.put("mobile", getSms().getPhoneNo());
        params.put("count", getSms().getWordCount());
        params.put("sum", getSms().getSmsNum());
        params.put("cId", getCompanyId());
        params.put("sId", getStoreId());
        params.put("channel", getSmsChannel().getChannel());
        params.put("status", getSendStatus().getStatus());
        return params;
    }

}
