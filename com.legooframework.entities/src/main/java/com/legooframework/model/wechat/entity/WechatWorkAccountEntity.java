package com.legooframework.model.wechat.entity;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;

public class WechatWorkAccountEntity extends BaseEntity<String> {

	public WechatWorkAccountEntity(WechatAccountEntity account) {
		super(null);
		this.account = account;
	}

	private WechatAccountEntity account;
	// 微信好友
	private final List<WechatFriendEntity> wechatFriends = Lists.newArrayList();
	// 标签组
	private final List<WechatLabelEntity> labels = Lists.newArrayList();

	public void addWechatFriend(WechatFriendEntity friend) {
		this.wechatFriends.add(friend);
	}

	public void addWechatFriends(Collection<? extends WechatFriendEntity> friends) {
		this.wechatFriends.addAll(friends);
	}

	public void addWechatLabel(WechatLabelEntity label) {
		this.labels.add(label);
	}

	public void addWechatLabels(Collection<? extends WechatLabelEntity> labels) {
		this.labels.addAll(labels);
	}

	public WechatAccountEntity getAccount() {
		return account;
	}

	public Optional<List<WechatAccountEntity>> getWechatFriendAccounts() {
		List<WechatAccountEntity> accounts = this.wechatFriends.stream().map(x -> x.getAccount())
				.collect(Collectors.toList());
		if (accounts.isEmpty())
			return Optional.empty();
		return Optional.of(accounts);
	}

	public Optional<List<WechatFriendEntity>> getWechatFriends() {
		return Optional.ofNullable(wechatFriends);
	}

	public Optional<List<WechatLabelEntity>> getLabels() {
		return Optional.ofNullable(labels);
	}

	public Map<WechatLabelEntity, List<WechatFriendEntity>> getLabelGroups() {
		List<WechatFriendEntity> friends = this.wechatFriends.stream().filter(x -> !x.getLables().isEmpty())
				.collect(Collectors.toList());
		Map<WechatLabelEntity, List<WechatFriendEntity>> group = Maps.newHashMap();
		if (friends.isEmpty())
			return group;
		if (this.labels.isEmpty())
			return group;
		for (WechatLabelEntity label : this.labels) {
			for (WechatFriendEntity friend : friends) {
				if (friend.getLables().contains(label)) {
					if (!group.containsKey(label)) {
						group.put(label, Lists.newArrayList(friend));
					} else {
						group.get(label).add(friend);
					}
				}
			}
		}
		Map<WechatLabelEntity, List<WechatFriendEntity>> sortMap = new TreeMap<WechatLabelEntity, List<WechatFriendEntity>>(
				new Comparator<WechatLabelEntity>() {
					@Override
					public int compare(WechatLabelEntity o1, WechatLabelEntity o2) {
						// TODO Auto-generated method stub
						return o1.getId().compareTo(o2.getId());
					}
				});
		sortMap.putAll(group);
		sortMap.entrySet().forEach(x -> Collections.sort(x.getValue()));
		return sortMap;
	}

	public Optional<List<WechatFriendEntity>> getFriendsByLabel(WechatLabelEntity label) {
		Objects.requireNonNull(label);
		List<WechatFriendEntity> friends = this.wechatFriends.stream().filter(x -> x.exitsLable(label))
				.collect(Collectors.toList());
		return Optional.ofNullable(friends);
	}

	public Optional<List<WechatAccountEntity>> getAllFriends() {
		List<WechatAccountEntity> friends = this.wechatFriends.stream().map(x -> x.getAccount())
				.collect(Collectors.toList());
		return Optional.ofNullable(friends);
	}

	@Override
	public String toString() {
		return "WechatWorkAccountEntity [account=" + account + ", wechatFriends=" + wechatFriends + ", labels=" + labels
				+ "]";
	}
}
