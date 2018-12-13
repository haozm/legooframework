package com.legooframework.model.wechat.entity;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.legooframework.model.core.jdbc.BatchSetter;

public class WechatFriendPO implements BatchSetter {

	private final Long accountId;

	private final String accountName;

	private final Long friendId;

	private final String conRemark;

	private final String encryptConRemark;

	private final String labelIds;

	private final int deleteFlag;

	private int operation = 0;// 0为插入操作 1为更新操作

	public WechatFriendPO(Long accountId, String accountName, Long friendId, String conRemark, String encryptConRemark,
			String labelIds, int deleteFlag) {
		super();
		this.accountId = accountId;
		this.accountName = accountName;
		this.friendId = friendId;
		this.conRemark = conRemark;
		this.encryptConRemark = encryptConRemark;
		this.labelIds = labelIds;
		this.deleteFlag = deleteFlag;
	}

	public void execUpdateOperation() {
		this.operation = 1;
	}

	public void execInsertOperation() {
		this.operation = 0;
	}

	public boolean isInsertOperation() {
		return this.operation == 0;
	}

	public boolean isUpdateOperation() {
		return this.operation == 1;
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		if (isInsertOperation())
			addValues(ps);
		if (isUpdateOperation())
			updateValues(ps);
	}

	private void addValues(PreparedStatement ps) throws SQLException {
		ps.setLong(1, this.accountId);
		ps.setString(2, this.accountName);
		ps.setLong(3, this.friendId);
		ps.setString(4, this.conRemark);
		ps.setString(5, this.encryptConRemark);
		ps.setString(6, this.labelIds);
		ps.setInt(7, this.deleteFlag);
	}

	private void updateValues(PreparedStatement ps) throws SQLException {
		ps.setString(1, this.accountName);
		ps.setString(2, this.conRemark);
		ps.setString(3, this.encryptConRemark);
		ps.setString(4, this.labelIds);
		ps.setLong(5, this.accountId);
		ps.setLong(6, this.friendId);
	}

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public Long getAccountId() {
		return accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public Long getFriendId() {
		return friendId;
	}

	public String getConRemark() {
		return conRemark;
	}

	public String getEncryptConRemark() {
		return encryptConRemark;
	}

	public String getLabelIds() {
		return labelIds;
	}

	public int getDeleteFlag() {
		return deleteFlag;
	}

	@Override
	public String toString() {
		return "WechatFriendPO [accountId=" + accountId + ", accountName=" + accountName + ", friendId=" + friendId
				+ ", conRemark=" + conRemark + ", encryptConRemark=" + encryptConRemark + ", labelIds=" + labelIds
				+ ", deleteFlag=" + deleteFlag + ", operation=" + operation + "]";
	}

}
