package com.legooframework.model.smsresult.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SMSReplayEntity extends BaseEntity<Long> implements BatchSetter {

    private final Long smsExt;
    private final String sendMsgId, mobile, content;
    private Integer companyId, storeId;
    private final LocalDateTime replayDate;

    private SMSReplayEntity(Long smsExt, String sendMsgId, String mobile, String content, LocalDateTime replayDate) {
        super(0L);
        this.smsExt = smsExt;
        this.sendMsgId = sendMsgId;
        this.mobile = mobile;
        this.content = content;
        this.replayDate = replayDate;
    }

    SMSReplayEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.mobile = ResultSetUtil.getString(res, "mobile");
            this.sendMsgId = ResultSetUtil.getString(res, "sendMsgId");
            this.content = ResultSetUtil.getString(res, "content");
            this.smsExt = res.getLong("smsExt");
            this.replayDate = LocalDateTime.fromDateFields(res.getTimestamp("replayDate"));
            this.companyId = ResultSetUtil.getOptObject(res, "companyId", Integer.class).orElse(-1);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(-1);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSReplayEntity has SQLException", e);
        }
    }

    SMSReplayEntity(ResultSet res) {
        super(0L);
        try {
            this.mobile = ResultSetUtil.getString(res, "mobile");
            this.companyId = ResultSetUtil.getOptObject(res, "companyId", Integer.class).orElse(-1);
            this.storeId = ResultSetUtil.getOptObject(res, "storeId", Integer.class).orElse(-1);
            this.sendMsgId = null;
            this.content = null;
            this.smsExt = null;
            this.replayDate = null;
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSReplayEntity has SQLException", e);
        }
    }

    String getSendMsgId() {
        return sendMsgId;
    }

    String getMobile() {
        return mobile;
    }

    String getContent() {
        return content;
    }

    Integer getCompanyId() {
        return companyId;
    }

    public LocalDateTime getReplayDate() {
        return replayDate;
    }

    Integer getStoreId() {
        return storeId;
    }

    static SMSReplayEntity createInstance(String replay_info) {
        // 578096,,18128509449,td,2019-04-02 17:23:59
        String[] args = StringUtils.splitByWholeSeparatorPreserveAllTokens(replay_info, ",");
        Preconditions.checkArgument(args.length == 5, "返回格式异常 ：%s", replay_info);
        LocalDateTime dateTime = DateTimeUtils.parseDef(args[4]);
        return new SMSReplayEntity(Long.valueOf(args[0]), args[1], args[2], WebUtils.decodeUrl(args[3]),
                dateTime);
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        phone_no, sms_ext, send_msg_id, sms_context,  createTime
        ps.setObject(1, this.mobile);
        ps.setObject(2, this.smsExt);
        ps.setObject(3, this.sendMsgId);
        ps.setObject(4, this.content);
        ps.setObject(5, this.replayDate.toDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SMSReplayEntity)) return false;
        SMSReplayEntity that = (SMSReplayEntity) o;
        return Objects.equal(smsExt, that.smsExt) &&
                Objects.equal(sendMsgId, that.sendMsgId) &&
                Objects.equal(mobile, that.mobile) &&
                Objects.equal(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(smsExt, sendMsgId, mobile, replayDate, content);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("smsExt", smsExt)
                .add("sendMsgId", sendMsgId)
                .add("mobile", mobile)
                .add("content", content)
                .add("replayDate", replayDate)
                .toString();
    }
}
