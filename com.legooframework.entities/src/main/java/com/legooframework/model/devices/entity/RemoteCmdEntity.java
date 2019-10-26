package com.legooframework.model.devices.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RemoteCmdEntity extends BaseEntity<Long> implements BatchSetter {

    private String tag, fromDeviceId, toDeviceId, command, batchNo, remark;
    private boolean sendFlag, successFlag;

    RemoteCmdEntity(String tag, String fromDeviceId, String toDeviceId, String command,
                    String batchNo, String remark) {
        super(0L);
        this.tag = tag;
        this.fromDeviceId = fromDeviceId;
        this.toDeviceId = toDeviceId;
        this.command = command;
        this.batchNo = batchNo;
        this.remark = Strings.isNullOrEmpty(remark) ? "web" : remark;
        this.sendFlag = false;
        this.successFlag = false;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, this.getTag());
        ps.setString(2, this.getFromDeviceId());
        ps.setString(3, this.getToDeviceId());
        ps.setString(4, this.getCommand());
        ps.setString(5, this.getBatchNo());
        ps.setString(6, this.getRemark());
    }

    public String getTag() {
        return tag;
    }

    public String getFromDeviceId() {
        return fromDeviceId;
    }

    public String getToDeviceId() {
        return toDeviceId;
    }

    public String getCommand() {
        return command;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public String getRemark() {
        return remark;
    }

    public boolean isSendFlag() {
        return sendFlag;
    }

    public boolean isSuccessFlag() {
        return successFlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RemoteCmdEntity that = (RemoteCmdEntity) o;
        return sendFlag == that.sendFlag &&
                successFlag == that.successFlag &&
                Objects.equal(tag, that.tag) &&
                Objects.equal(fromDeviceId, that.fromDeviceId) &&
                Objects.equal(toDeviceId, that.toDeviceId) &&
                Objects.equal(command, that.command) &&
                Objects.equal(batchNo, that.batchNo) &&
                Objects.equal(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), tag, fromDeviceId, toDeviceId, command, batchNo,
                remark, sendFlag, successFlag);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tag", tag)
                .add("fromDeviceId", fromDeviceId)
                .add("toDeviceId", toDeviceId)
                .add("command", command)
                .add("batchNo", batchNo)
                .add("remark", remark)
                .add("sendFlag", sendFlag)
                .add("successFlag", successFlag)
                .toString();
    }
}
