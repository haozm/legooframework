package com.legooframework.model.wechat.entity;

import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;

public class WechatLabelEntity extends BaseEntity<String> {

	private final WechatAccountEntity onwer;
	// 标签名称
	private String name;
	// 标签名称全拼
	private String pyFull;
	// 标签名称简称
	private String pyShort;
	// 是否临时
	private int isTemporary;

	WechatLabelEntity(WechatAccountEntity onwer, String id, String name, String pyFull, String pyShort,
			int isTemporary) {
		super(id);
		this.onwer = onwer;
		this.name = name.replaceAll("'", "’").replaceAll("\\\\", "/");
		this.pyFull = pyFull;
		this.pyShort = pyShort;
		this.isTemporary = isTemporary;
	}

	WechatLabelEntity(WechatLabelEntity orgin) {
		super(orgin.getId());
		this.onwer = orgin.onwer;
		this.name = orgin.getName().replaceAll("'", "’").replaceAll("\\", "/");
		this.pyFull = orgin.getPyFull();
		this.pyShort = orgin.getPyShort();
		this.isTemporary = orgin.getIsTemporary();
	}

	public WechatLabelEntity modify(String name, String pyFull, String pyShort, int isTemporary) {
		WechatLabelEntity clone = new WechatLabelEntity(this);
		clone.name = name.replaceAll("'", "’").replaceAll("\\", "/");
		clone.pyFull = pyFull;
		clone.pyShort = pyShort;
		clone.isTemporary = isTemporary;
		return clone;
	}

	public String getName() {
		return name;
	}

	public String getPyFull() {
		return pyFull;
	}

	public String getPyShort() {
		return pyShort;
	}

	public int getIsTemporary() {
		return isTemporary;
	}

	public WechatAccountEntity getOnwer() {
		return onwer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + isTemporary;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((onwer == null) ? 0 : onwer.hashCode());
		result = prime * result + ((pyFull == null) ? 0 : pyFull.hashCode());
		result = prime * result + ((pyShort == null) ? 0 : pyShort.hashCode());
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
		WechatLabelEntity other = (WechatLabelEntity) obj;
		if (!Strings.nullToEmpty(this.getId()).equals(Strings.nullToEmpty(other.getId())))
			return false;
		if (isTemporary != other.isTemporary)
			return false;
		if (!Strings.nullToEmpty(name).equals(Strings.nullToEmpty(other.name)))
			return false;
		if (!Strings.nullToEmpty(pyFull).equals(Strings.nullToEmpty(other.pyFull)))
			return false;
		if (!Strings.nullToEmpty(pyShort).equals(Strings.nullToEmpty(other.pyShort)))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WechatLabelEntity [onwer=" + onwer + ", name=" + name + ", pyFull=" + pyFull + ", pyShort=" + pyShort
				+ ", isTemporary=" + isTemporary + "]";
	}

}
