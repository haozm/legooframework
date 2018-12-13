package com.legooframework.model.wechat.entity;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.legooframework.model.core.jdbc.BatchSetter;

public class WechatAccountPO implements BatchSetter {

	private final Long id;
	// 用户名
	private final String userName;
	// 编码后的用户名
	private final String encryptUsername;
	// 微信用户头像
	private final String iconUrl;
	// 微信备注
	private final String conRemark;
	// 编码后的备注
	private final String encryptConRemark;
	// 昵称
	private final String nickName;
	// 编码后的昵称
	private final String encryptNickName;
	// 微信类型
	private final int type;

	private int operation = 0;// 0为插入操作 1为更新操作

	WechatAccountPO(Long id, String userName, String encryptUsername, String iconUrl, String conRemark,
			String encryptConRemark, String nickName, String encryptNickName, int type) {
		super();
		this.id = id;
		this.userName = userName;
		this.encryptUsername = encryptUsername;
		this.iconUrl = iconUrl;
		this.conRemark = conRemark;
		this.encryptConRemark = encryptConRemark;
		this.nickName = nickName;
		this.encryptNickName = encryptNickName;
		this.type = type;
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

	WechatAccountPO(WechatAccountEntity account) {
		this.id = account.getId();
		this.userName = account.getUserName();
		this.encryptUsername = account.getEncryptUsername();
		this.iconUrl = account.getIconUrl();
		this.conRemark = account.getConRemark();
		this.encryptConRemark = account.getEncryptConRemark();
		this.nickName = account.getNickName();
		this.encryptNickName = account.getEncryptNickName();
		this.type = account.getType();
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		if (isInsertOperation())
			setInsertValue(ps);
		if (isUpdateOperation())
			setUpdateValue(ps);
	}

	private void setInsertValue(PreparedStatement ps) throws SQLException {
		ps.setLong(1, this.id);
		ps.setString(2, this.userName);
		ps.setString(3, this.encryptUsername);
		ps.setString(4, this.iconUrl);
		ps.setString(5, this.nickName);
		ps.setString(6, this.encryptNickName);
		ps.setInt(7, this.type);
	}

	private void setUpdateValue(PreparedStatement ps) throws SQLException {
		ps.setString(1, this.iconUrl);
		ps.setString(2, this.nickName);
		ps.setString(3, this.encryptNickName);
		ps.setInt(4, this.type);
		ps.setLong(5, this.id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conRemark == null) ? 0 : conRemark.hashCode());
		result = prime * result + ((encryptConRemark == null) ? 0 : encryptConRemark.hashCode());
		result = prime * result + ((encryptNickName == null) ? 0 : encryptNickName.hashCode());
		result = prime * result + ((encryptUsername == null) ? 0 : encryptUsername.hashCode());
		result = prime * result + ((iconUrl == null) ? 0 : iconUrl.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((nickName == null) ? 0 : nickName.hashCode());
		result = prime * result + type;
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WechatAccountPO other = (WechatAccountPO) obj;
		if (conRemark == null) {
			if (other.conRemark != null)
				return false;
		} else if (!conRemark.equals(other.conRemark))
			return false;
		if (encryptConRemark == null) {
			if (other.encryptConRemark != null)
				return false;
		} else if (!encryptConRemark.equals(other.encryptConRemark))
			return false;
		if (encryptNickName == null) {
			if (other.encryptNickName != null)
				return false;
		} else if (!encryptNickName.equals(other.encryptNickName))
			return false;
		if (encryptUsername == null) {
			if (other.encryptUsername != null)
				return false;
		} else if (!encryptUsername.equals(other.encryptUsername))
			return false;
		if (iconUrl == null) {
			if (other.iconUrl != null)
				return false;
		} else if (!iconUrl.equals(other.iconUrl))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (nickName == null) {
			if (other.nickName != null)
				return false;
		} else if (!nickName.equals(other.nickName))
			return false;
		if (type != other.type)
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WechatAccountPO [id=" + id + ", userName=" + userName + ", encryptUsername=" + encryptUsername
				+ ", iconUrl=" + iconUrl + ", conRemark=" + conRemark + ", encryptConRemark=" + encryptConRemark
				+ ", nickName=" + nickName + ", encryptNickName=" + encryptNickName + ", type=" + type + "]";
	}

}
