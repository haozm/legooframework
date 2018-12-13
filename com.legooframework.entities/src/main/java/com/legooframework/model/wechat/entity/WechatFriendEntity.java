package com.legooframework.model.wechat.entity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;

public class WechatFriendEntity extends BaseEntity<String> implements Comparable<WechatFriendEntity> {

	private WechatAccountEntity onwer;

	private WechatAccountEntity account;

	private final List<WechatLabelEntity> lables = Lists.newArrayList();

	WechatFriendEntity(WechatAccountEntity onwer, WechatAccountEntity account, List<WechatLabelEntity> labels) {
		super(null);
		Objects.requireNonNull(onwer);
		Objects.requireNonNull(account);
		Objects.requireNonNull(labels);
		this.onwer = onwer;
		this.account = account;
		if (!labels.isEmpty())
			this.addLables(labels);
	}

	void addLable(WechatLabelEntity label) {
		this.lables.add(label);
	}

	void addLables(Collection<? extends WechatLabelEntity> labels) {
		this.lables.addAll(labels);
	}

	public WechatAccountEntity getOnwer() {
		return onwer;
	}

	public void setOnwer(WechatAccountEntity onwer) {
		this.onwer = onwer;
	}

	public WechatAccountEntity getAccount() {
		return account;
	}

	public void setAccount(WechatAccountEntity account) {
		this.account = account;
	}

	public List<WechatLabelEntity> getLables() {
		return lables;
	}

	public boolean exitsLable(WechatLabelEntity label) {
		if (label == null || lables.isEmpty())
			return false;
		return lables.contains(label);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((lables == null) ? 0 : lables.hashCode());
		result = prime * result + ((onwer == null) ? 0 : onwer.hashCode());
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
		WechatFriendEntity other = (WechatFriendEntity) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (lables == null) {
			if (other.lables != null)
				return false;
		} else if (!lables.stream().map(x -> x.getId()).collect(Collectors.toList())
				.containsAll(other.lables.stream().map(x -> x.getId()).collect(Collectors.toList())))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WechatFriendEntity [onwer=" + onwer + ", account=" + account + ", lables=" + lables + "]";
	}

	@Override
	public int compareTo(WechatFriendEntity o) {
		return this.account.getUserName().compareTo(o.getAccount().getUserName());
	}

}
