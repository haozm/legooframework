package com.legooframework.model.wechat.entity;

import com.legooframework.model.core.base.entity.BaseEntity;

public class WechatCircleUpvoteEntity extends BaseEntity<String> {

	private final WechatAccountEntity account;

	private final WechatCircleEntity circle;

	private final UpvoteType type;

	private final String content;

	private final long upvoteTime;

	public WechatCircleUpvoteEntity(String id, WechatAccountEntity account, WechatCircleEntity circle, UpvoteType type,
			String content, long upvoteTime) {
		super(id);
		this.account = account;
		this.circle = circle;
		this.type = type;
		this.content = content;
		this.upvoteTime = upvoteTime;
	}

	public static enum UpvoteType {
		UPVOTE("点赞", 1), REPLY_TEXT("文字回复", 2);
		private final String name;
		private final int value;

		private UpvoteType(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public static UpvoteType valueOf(int val) {
			for (UpvoteType type : values()) {
				if (type.value == val)
					return type;
			}
			return null;
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}
	}

	public static enum Operation {
		UNREVOKE("未撤销", 0), REVOKE("撤销", 1);
		private final String name;
		private final int value;

		private Operation(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public static Operation valueOf(int val) {
			for (Operation oper : values()) {
				if (oper.value == val)
					return oper;
			}
			return null;
		}
	}

	public WechatAccountEntity getAccount() {
		return account;
	}

	public WechatCircleEntity getCircle() {
		return circle;
	}

	public UpvoteType getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public long getUpvoteTime() {
		return upvoteTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((circle == null) ? 0 : circle.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (int) (upvoteTime ^ (upvoteTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WechatCircleUpvoteEntity other = (WechatCircleUpvoteEntity) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (circle == null) {
			if (other.circle != null)
				return false;
		} else if (!circle.equals(other.circle))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (type != other.type)
			return false;
		if (upvoteTime != other.upvoteTime)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WechatCircleUpvoteEntity [account=" + account + ", circle=" + circle + ", type=" + type + ", content="
				+ content + ", upvoteTime=" + upvoteTime + "]";
	}

}
