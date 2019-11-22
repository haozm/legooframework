package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SendMsg4FinalEntity extends BaseEntity<String> implements BatchSetter {
    //18588828127|233b12b8-ad15-4637-8a00-8f97a4ab90ee|9,0,1,2|2019-11-12 21:41:49|UNDELIV
    private FinalState finalState;
    private LocalDateTime finalStateDate;
    private String finalStateDesc;

    SendMsg4FinalEntity(String id, ResultSet res) {
        super(id);
        try {
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
        ps.setObject(1, this.finalState.getState());
        ps.setObject(2, this.finalStateDate.toDate());
        ps.setObject(3, this.finalStateDesc);
        ps.setObject(4, this.getId());
    }

    private SendMsg4FinalEntity(String id, int finalState, String finalStateDate, String finalStateDesc) {
        super(id);
        this.finalState = FinalState.paras(finalState);
        this.finalStateDate = DateTimeUtils.parseDef(finalStateDate);
        this.finalStateDesc = finalStateDesc;
    }

    public static SendMsg4FinalEntity create(String id, int recCode, String recDate, String recDesc) {
        SendMsg4FinalEntity res;
        switch (recCode) {
            case 1:
                res = new SendMsg4FinalEntity(id, FinalState.DELIVRD.getState(), recDate, recDesc);
                break;
            case 2:
                res = new SendMsg4FinalEntity(id, FinalState.UNDELIV.getState(), recDate, recDesc);
                break;
            default:
                throw new IllegalArgumentException(String.format("非法参数 recCode=%s", recCode));
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendMsg4FinalEntity)) return false;
        SendMsg4FinalEntity that = (SendMsg4FinalEntity) o;
        return finalState == that.finalState &&
                Objects.equal(this.getId(), that.getId()) &&
                Objects.equal(finalStateDate, that.finalStateDate) &&
                Objects.equal(finalStateDesc, that.finalStateDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId(), finalState, finalStateDate, finalStateDesc);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("finalState", finalState)
                .add("finalStateDate", finalStateDate)
                .add("finalStateDesc", finalStateDesc)
                .toString();
    }
}
