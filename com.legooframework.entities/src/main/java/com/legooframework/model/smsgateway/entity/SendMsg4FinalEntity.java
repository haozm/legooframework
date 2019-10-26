package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.smsresult.entity.FinalState;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SendMsg4FinalEntity extends BaseEntity<String> implements BatchSetter {

    private SendStatus sendStatus;
    private FinalState finalState;
    private LocalDateTime finalStateDate;
    private String finalStateDesc;

    SendMsg4FinalEntity(String id, ResultSet res) {
        super(id);
        try {
            this.sendStatus = SendStatus.paras(ResultSetUtil.getObject(res, "sendStatus", Integer.class));
            this.finalState = FinalState.paras(ResultSetUtil.getObject(res, "finalState", Integer.class));
            this.finalStateDate = res.getObject("finalStateDate") == null ? null :
                    LocalDateTime.fromDateFields(res.getTimestamp("finalStateDate"));
            this.finalStateDesc = ResultSetUtil.getOptString(res, "finalStateDesc", null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SendMsg4FinalEntity has SQLException", e);
        }
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setObject(1, this.sendStatus.getStatus());
        ps.setObject(2, this.finalState.getState());
        ps.setObject(3, this.finalStateDate.toDate());
        ps.setObject(4, this.finalStateDesc);
        ps.setObject(5, this.getId());
    }

    private SendMsg4FinalEntity(String id, int sendStatus, int finalState, String finalStateDate, String finalStateDesc) {
        super(id);
        this.sendStatus = SendStatus.paras(sendStatus);
        this.finalState = FinalState.paras(finalState);
        this.finalStateDate = DateTimeUtils.parseDef(finalStateDate);
        this.finalStateDesc = finalStateDesc;
    }

    public static SendMsg4FinalEntity getInstance(String id, int sendStatus, int finalState, String finalStateDate,
                                                  String finalStateDesc) {
        SendStatus status = SendStatus.paras(sendStatus);
        Preconditions.checkState(SendStatus.SendedGateWay == status || SendStatus.SMS4SendError == status,
                "错误的发送状态%s", sendStatus);
        FinalState state = FinalState.paras(finalState);
        Preconditions.checkState(FinalState.DELIVRD == state || FinalState.UNDELIV == state,
                "错误的发送状态%s", finalState);
        return new SendMsg4FinalEntity(id, sendStatus, finalState, finalStateDate, finalStateDesc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendMsg4FinalEntity)) return false;
        SendMsg4FinalEntity that = (SendMsg4FinalEntity) o;
        return sendStatus == that.sendStatus &&
                finalState == that.finalState &&
                Objects.equal(this.getId(), that.getId()) &&
                Objects.equal(finalStateDate, that.finalStateDate) &&
                Objects.equal(finalStateDesc, that.finalStateDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId(), sendStatus, finalState, finalStateDate, finalStateDesc);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("sendStatus", sendStatus)
                .add("finalState", finalState)
                .add("finalStateDate", finalStateDate)
                .add("finalStateDesc", finalStateDesc)
                .toString();
    }
}
