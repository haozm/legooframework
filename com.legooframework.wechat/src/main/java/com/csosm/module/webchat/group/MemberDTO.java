package com.csosm.module.webchat.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.Maps;

public class MemberDTO {

	private final String userId;

	private String memberId = "";

	private String memberName = "";

	private final String iconUrl;

	private final String userName;

	private final String nickName;

	private final String conRemark;

	private int type;
	
	private int signed;

	public MemberDTO(String userId, String iconUrl, String userName, String nickName, String conRemark, int type) {
		super();
		this.userId = userId;
		this.iconUrl = iconUrl;
		this.userName = userName;
		this.nickName = nickName;
		this.conRemark = conRemark;
		this.type = type;
	}

	public MemberDTO(String userId, String iconUrl, String userName, String nickName, String conRemark, int type,
			int signed,String memberId, String memberName) {
		super();
		this.userId = userId;
		this.iconUrl = iconUrl;
		this.userName = userName;
		this.nickName = nickName;
		this.conRemark = conRemark;
		this.type = type;
		this.signed = signed;
		this.memberId = memberId == null ? "" : memberId;
		this.setMemberName(memberName == null ? "" : memberName);
	}

	public static MemberDTO valueOf(ResultSet rs) {
		try {
			String userId = rs.getString("userName");
			String userName = rs.getString("userName");
			String iconUrl = rs.getString("iconUrl");
			String nickName = rs.getString("nickName");
			String conRemark = rs.getString("conRemark");
			String memberId = rs.getString("memberId");
			String memberName = rs.getString("memberName");
			int signed = rs.getInt("signed");
			int type = rs.getInt("wxType");
			return new MemberDTO(userId, iconUrl, userName, nickName, conRemark, type,signed, memberId, memberName);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("数据库记录转换MemberDTO异常");
		}
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getUserName() {
		return userName;
	}

	public String getNickName() {
		return nickName;
	}

	public String getConRemark() {
		return conRemark;
	}

	public int getSigned() {
		return this.signed;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conRemark == null) ? 0 : conRemark.hashCode());
		result = prime * result + ((iconUrl == null) ? 0 : iconUrl.hashCode());
		result = prime * result + ((memberId == null) ? 0 : memberId.hashCode());
		result = prime * result + ((nickName == null) ? 0 : nickName.hashCode());
		result = prime * result + type;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		MemberDTO other = (MemberDTO) obj;
		if (conRemark == null) {
			if (other.conRemark != null)
				return false;
		} else if (!conRemark.equals(other.conRemark))
			return false;
		if (iconUrl == null) {
			if (other.iconUrl != null)
				return false;
		} else if (!iconUrl.equals(other.iconUrl))
			return false;
		if (memberId == null) {
			if (other.memberId != null)
				return false;
		} else if (!memberId.equals(other.memberId))
			return false;
		if (nickName == null) {
			if (other.nickName != null)
				return false;
		} else if (!nickName.equals(other.nickName))
			return false;
		if (type != other.type)
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
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
		return "MemberDTO [userId=" + userId + ", memberId=" + memberId + ", iconUrl=" + iconUrl + ", userName="
				+ userName + ", nickName=" + nickName + ", conRemark=" + conRemark + ", type=" + type + "]";
	}

	public Map<String, Object> toMap() {
		Map<String, Object> children = Maps.newHashMap();
		children.put("weixinId", this.getUserId());
		children.put("userId", this.getUserId());
		children.put("memberId", this.getMemberId());
		children.put("memberName", this.getMemberName());
		children.put("iconUrl", this.getIconUrl());
		children.put("userName", this.getUserName());
		children.put("nickName", this.getNickName());
		children.put("conRemark", this.getConRemark());
		children.put("id", this.getUserId());
		children.put("label", this.getNickName());
		children.put("type", this.getType());
		children.put("signed", this.signed);
		return children;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

}
