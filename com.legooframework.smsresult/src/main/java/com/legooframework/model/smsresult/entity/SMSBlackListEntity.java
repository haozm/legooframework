package com.legooframework.model.smsresult.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class SMSBlackListEntity extends BaseEntity<String> implements BatchSetter {

    private final Integer companyId, storeId;
    private final String mobile, content, sendMsgId;
    private final LocalDateTime replayDate;

    private SMSBlackListEntity(Integer companyId, Integer storeId, String mobile, String content) {
        super("");
        this.companyId = companyId == null ? -1 : companyId;
        this.storeId = storeId == null ? -1 : storeId;
        this.mobile = mobile;
        this.replayDate = LocalDateTime.now();
        this.sendMsgId = null;
        this.content = content;
    }

    SMSBlackListEntity(String id, ResultSet res) {
        super(id);
        try {
            this.mobile = ResultSetUtil.getString(res, "mobile");
            this.sendMsgId = null;
            this.content = ResultSetUtil.getString(res, "content");
            this.replayDate = null;
            this.companyId = ResultSetUtil.getOptObject(res, "companyId", Integer.class).orElse(-1);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(-1);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSReplayEntity has SQLException", e);
        }
    }

    public static SMSBlackListEntity creatInstance(SMSReplyEntity replayInfo) {
        return new SMSBlackListEntity(replayInfo.getCompanyId(), replayInfo.getStoreId(), replayInfo.getMobile(), "退订回T");
    }

    public boolean enabled() {
        return companyId != -1 && storeId != -1;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        company_id, store_id, phone_no, send_msg_id, sms_context,  sms_replay_date
        ps.setObject(1, this.companyId);
        ps.setObject(2, this.storeId);
        ps.setObject(3, this.mobile);
        ps.setObject(4, this.sendMsgId);
        ps.setObject(5, this.content);
        ps.setObject(6, this.replayDate.toDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSBlackListEntity)) return false;
        SMSBlackListEntity that = (SMSBlackListEntity) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(mobile, that.mobile) &&
                Objects.equal(content, that.content) &&
                Objects.equal(sendMsgId, that.sendMsgId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, storeId, mobile, content, sendMsgId);
    }


    public String toViewPayload() {
        return String.format("%s|%s|%s|%s", this.companyId, this.storeId, this.mobile, this.content);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("mobile", mobile)
                .add("content", content)
                .add("sendMsgId", sendMsgId)
                .add("replayDate", replayDate)
                .toString();
    }
}
