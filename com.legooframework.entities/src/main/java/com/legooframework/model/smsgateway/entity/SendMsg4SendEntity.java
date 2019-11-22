package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.joda.time.DateTime;

import java.util.Date;

public class SendMsg4SendEntity extends BaseEntity<String> {

    private String sendResCode, sendRemarks;
    private Date sendLocalDate;
    private SendStatus sendStatus;

    public static SendMsg4SendEntity createSMS4SendError(String smsId, String remarks) {
        SendMsg4SendEntity entity = new SendMsg4SendEntity(smsId);
        entity.sendStatus = SendStatus.Msg4SendError;
        entity.sendResCode = "9999";
        entity.sendRemarks = Strings.isNullOrEmpty(remarks) ? "发送信息失败" : remarks;
        return entity;
    }


    public SendMsg4SendEntity(String id) {
        super(id);
    }

    String getSendResCode() {
        return sendResCode;
    }

    SendStatus getSendStatus() {
        return sendStatus;
    }

    String getSendRemarks() {
        return sendRemarks;
    }

    Date getSendLocalDate() {
        return sendLocalDate;
    }

    public boolean isError() {
        return SendStatus.Msg4SendError == getSendStatus();
    }

    public void errorByException(Exception e) {
        Preconditions.checkState(SendStatus.Msg4Sending == getSendStatus());
        this.sendStatus = SendStatus.Msg4SendError;
        this.sendResCode = "9999";
        String _msg = e.getMessage();
        this.sendRemarks = _msg != null && _msg.length() > 512 ? _msg.substring(0, 511) : _msg;
    }

    public void error4Encode() {
        Preconditions.checkState(SendStatus.Msg4Sending == getSendStatus());
        this.sendStatus = SendStatus.Msg4SendError;
        this.sendResCode = "7102";
        this.sendRemarks = "编码为UNCODE码无效";
    }

    public void error4Init() {
        Preconditions.checkState(SendStatus.Msg4Sending == getSendStatus());
        this.sendStatus = SendStatus.Msg4SendError;
        this.sendResCode = "7104";
        this.sendRemarks = "短信生成异常";
    }

    public void errorByBlackList() {
        Preconditions.checkState(SendStatus.Msg4Sending == getSendStatus());
        this.sendStatus = SendStatus.Msg4SendError;
        this.sendResCode = "7101";
        this.sendRemarks = "号码设置无效或者回复TD退订";
    }

    public void errorByMobile() {
        Preconditions.checkState(SendStatus.Msg4Sending == getSendStatus());
        this.sendStatus = SendStatus.Msg4SendError;
        this.sendResCode = "7100";
        this.sendRemarks = "非法的电话或者号码无效";
    }

    public void finshedSend() {
        this.sendStatus = SendStatus.Msg4Sended;
        this.sendResCode = "0000";
        this.sendLocalDate = DateTime.now().toDate();
        this.sendRemarks = "提交网关完成";
    }

    public void errorBySending(String msg) {
        Preconditions.checkState(SendStatus.Msg4Sending == getSendStatus());
        this.sendStatus = SendStatus.Msg4SendError;
        this.sendResCode = "9999";
        this.sendRemarks = Strings.isNullOrEmpty(msg) ? "上送信息到网关失败" : msg;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sendResCode", sendResCode)
                .add("sendRemarks", sendRemarks)
                .add("sendLocalDate", sendLocalDate)
                .add("sendStatus", sendStatus)
                .toString();
    }
}
