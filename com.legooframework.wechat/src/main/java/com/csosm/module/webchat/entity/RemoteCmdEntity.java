package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.util.MyWebUtil;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class RemoteCmdEntity extends BaseEntity<Long> {

    private String tag, fromDeviceId, toDeviceId, command, batchNo, remark;
    private boolean sendFlag, successFlag;

    public RemoteCmdEntity(String tag, String fromDeviceId, String toDeviceId, String command,
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

    RemoteCmdEntity(Long id, ResultSet resultSet) throws SQLException {
        super(id, 0L, resultSet.getDate("createTime"));
        this.tag = resultSet.getString("tag");
        this.fromDeviceId = resultSet.getString("fromDeviceId");
        this.toDeviceId = resultSet.getString("toDeviceId");
        this.command = resultSet.getString("command");
        this.sendFlag = resultSet.getInt("sendFlag") == 1;
        this.successFlag = resultSet.getInt("successFlag") == 1;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> res = super.toMap();
        res.put("tag", tag);
        res.put("fromDeviceId", fromDeviceId);
        res.put("toDeviceId", toDeviceId);
        res.put("command", command);
        res.put("sendFlag", sendFlag);
        res.put("successFlag", successFlag);
        return res;
    }

    public static RemoteCmdEntity addPhoneContact(WechatAddFriendPushListEntity pushs, DevicesEntity device) 
    { 
        Map<String, Object> content = Maps.newHashMap();
        content.put("ids", String.format("%s,%s", device.getCompanyId(), device.getStoreId()));
        content.put("tag", "add_phone_contact");
        return new RemoteCmdEntity("add_phone_contact", String.format("EMP_%s", pushs.getUuid()),
                device.getId(), MyWebUtil.toJson(content), pushs.getUuid(), "web");
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
		return "RemoteCmdEntity [tag=" + tag + ", fromDeviceId=" + fromDeviceId + ", toDeviceId=" + toDeviceId
				+ ", command=" + command + ", batchNo=" + batchNo + ", remark=" + remark + ", sendFlag=" + sendFlag
				+ ", successFlag=" + successFlag + "]";
	}
   
}
