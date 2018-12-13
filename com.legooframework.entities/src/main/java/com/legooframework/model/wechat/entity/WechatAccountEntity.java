package com.legooframework.model.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;

public class WechatAccountEntity extends BaseEntity<Long> implements Comparable<WechatAccountEntity> {

	public final static String WECHAT_CACHE_NAME = "WechatCache";
	// 用户名
	private final String userName;
	// 编码后的用户名
	private final String encryptUsername;
	// 微信用户头像
	private final String iconUrl;
	// 微信备注
	private final String conRemark;
	// 编码后的备注
	private String encryptConRemark;
	// 昵称
	private String nickName;
	// 编码后的昵称
	private String encryptNickName;
	// 微信类型
	private final int type;

	public WechatAccountEntity(String userName, String iconUrl, String conRemark, String nickName, int type) {
		super(generateId(userName));
		if (Strings.isNullOrEmpty(userName))
			throw new IllegalArgumentException("userName 不能为空或null");
		this.userName = userName;
		this.encryptUsername = Base64.encodeBase64String(userName.getBytes());
		this.iconUrl = iconUrl;
		this.conRemark = conRemark == null ? "" : conRemark.replaceAll("'", "’").replaceAll("\\\\", "/");
		if (!Strings.isNullOrEmpty(conRemark))
			this.encryptConRemark = Base64.encodeBase64String(conRemark.getBytes());
		this.nickName = nickName == null ? "" : nickName.replaceAll("'", "’").replaceAll("\\\\", "/");
		if (!Strings.isNullOrEmpty(nickName))
			this.encryptNickName = Base64.encodeBase64String(nickName.getBytes());
		this.type = type;
	}

	WechatAccountEntity(Long id, String userName, String encryptUsername, String iconUrl, String conRemark,
			String encryptConRemark, String nickName, String encryptNickName, int type) {
		super(id);
		this.userName = userName;
		this.encryptUsername = encryptUsername;
		this.iconUrl = iconUrl;
		this.conRemark = conRemark == null ? "" : conRemark.replaceAll("'", "’").replaceAll("\\\\", "/");
		this.encryptConRemark = encryptConRemark;
		this.nickName = nickName == null ? "" : nickName.replaceAll("'", "’").replaceAll("\\\\", "/");
		this.encryptNickName = encryptNickName;
		this.type = type;
	}

	private static Long generateId(String userName) {
		return Long.valueOf(userName.hashCode());
	}

	public Long getLongId() {
		return super.getId().longValue();
	}

	public String getUserName() {
		return userName;
	}

	public String getEncryptUsername() {
		return encryptUsername;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getConRemark() {
		return conRemark;
	}

	public String getEncryptConRemark() {
		return encryptConRemark;
	}

	public String getNickName() {
		return nickName;
	}

	public String getEncryptNickName() {
		return encryptNickName;
	}

	public int getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		WechatAccountEntity that = (WechatAccountEntity) o;
		return type == that.type && Objects.equal(userName, that.userName)
				&& Objects.equal(encryptUsername, that.encryptUsername) && Objects.equal(iconUrl, that.iconUrl)
				&& Objects.equal(conRemark, that.conRemark) && Objects.equal(encryptConRemark, that.encryptConRemark)
				&& Objects.equal(nickName, that.nickName) && Objects.equal(encryptNickName, that.encryptNickName);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), userName, encryptUsername, iconUrl, conRemark, encryptConRemark,
				nickName, encryptNickName, type);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("userName", userName).add("encryptUsername", encryptUsername)
				.add("iconUrl", iconUrl).add("conRemark", conRemark).add("encryptConRemark", encryptConRemark)
				.add("nickName", nickName).add("encryptNickName", encryptNickName).add("type", type).toString();
	}

	@Override
	public int compareTo(WechatAccountEntity o) {
		return this.getUserName().compareTo(o.getUserName());
	}

}
