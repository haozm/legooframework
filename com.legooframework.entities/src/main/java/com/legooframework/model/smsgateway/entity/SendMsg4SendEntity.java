package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.joda.time.DateTime;

import java.util.Date;

public class SendMsg4SendEntity extends BaseEntity<String> {

    private String sendResCode, remarks;
    private Date sendLocalDate;
    private SendStatus sendStatus;

    public SendMsg4SendEntity(String id) {
        super(id);
    }

    String getSendResCode() {
        return sendResCode;
    }

    SendStatus getSendStatus() {
        return sendStatus;
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

    public void errorByException(Exception e) {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        this.sendStatus = SendStatus.SMS4SendError;
        this.sendResCode = "9999";
        String _msg = e.getMessage();
        this.remarks = _msg != null && _msg.length() > 512 ? _msg.substring(0, 511) : _msg;
    }

    public void error4Encode() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        this.sendStatus = SendStatus.SMS4SendError;
        this.sendResCode = "7102";
        this.remarks = "编码为UNCODE码无效";
    }

    public void error4Init() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        this.sendStatus = SendStatus.SMS4SendError;
        this.sendResCode = "7104";
        this.remarks = "短信生成异常";
    }

    public void errorByBlackList() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        this.sendStatus = SendStatus.SMS4SendError;
        this.sendResCode = "7101";
        this.remarks = "号码设置无效或者回复TD退订";
    }

    public void errorByMobile() {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        this.sendStatus = SendStatus.SMS4SendError;
        this.sendResCode = "7100";
        this.remarks = "非法的电话或者号码无效";
    }

    public void finshedSend() {
        this.sendStatus = SendStatus.SendedGateWay;
        this.sendResCode = "0000";
        this.sendLocalDate = DateTime.now().toDate();
        this.remarks = "提交网关完成";
    }

    public void errorBySending(String msg) {
        Preconditions.checkState(SendStatus.SMS4Sending == getSendStatus());
        this.sendStatus = SendStatus.SMS4SendError;
        this.sendResCode = "9999";
        this.remarks = Strings.isNullOrEmpty(msg) ? "上送信息到网关失败" : msg;
    }

}
