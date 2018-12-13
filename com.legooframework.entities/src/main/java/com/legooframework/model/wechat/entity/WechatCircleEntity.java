package com.legooframework.model.wechat.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;

public class WechatCircleEntity extends BaseEntity<String> {

	// 微信账号
	private final WechatAccountEntity account;
	// 朋友圈图片
	private final List<ImageEntity> images = Lists.newArrayList();
	// 朋友圈内容类型
	private final ContentType type;
	// 朋友圈内容
	private final String text;

	WechatCircleEntity(String id, WechatAccountEntity account, List<ImageEntity> images, ContentType type,
			String text) {
		super(id);
		this.account = account;
		this.images.addAll(images);
		this.type = type;
		this.text = text;
	}

	public static class ImageEntity implements Comparable<Integer> {

		private final String url;

		private final int index;

		ImageEntity(String url, int index) {
			this.url = url;
			this.index = index;
		}

		@Override
		public int compareTo(Integer o) {

			return this.index - o;
		}

		public String getUrl() {
			return url;
		}

		public int getIndex() {
			return index;
		}

	}

	public static enum ContentType {
		IMAGE("图片", 1), TEXT("文字", 2), IMAGE_AND_TEXT("图片与文字", 3);

		private final String name;
		private final int value;

		private ContentType(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public static ContentType valueOf(int val) {
			for (ContentType type : values()) {
				if (val == type.value)
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

	public WechatAccountEntity getAccount() {
		return account;
	}

	public List<ImageEntity> getImages() {
		return images;
	}

	public ContentType getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((images == null) ? 0 : images.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		WechatCircleEntity other = (WechatCircleEntity) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (images == null) {
			if (other.images != null)
				return false;
		} else if (!images.equals(other.images))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WechatCircleEntity [account=" + account + ", images=" + images + ", type=" + type + ", text=" + text
				+ "]";
	}

}
