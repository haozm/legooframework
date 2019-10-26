package com.csosm.module.webchat.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AllFriendGroupLogEntity extends BaseEntity<String>{
	
	private String groupId;
	
	private String groupName;
	
	private TreeSet<Integer> grantedStoreIds = Sets.newTreeSet();
	
	
	protected AllFriendGroupLogEntity(String id) {
		super(id);
	}
	
	public AllFriendGroupLogEntity(WeixinGroupEntity group) {
		super(UUID.randomUUID().toString().replaceAll("-", ""));
		this.groupId = group.getId();
		this.groupName = group.getName();
	}
	
	public AllFriendGroupLogEntity(String groupId, String groupName) {
		super(UUID.randomUUID().toString().replaceAll("-", ""));
		this.groupId = groupId;
		this.groupName = groupName;
	}
	
	private void addStoreIds(String ... storeIds) {
		if(storeIds.length == 0)
			return ;
		for(int i = 0 ; i < storeIds.length; i++) {
			grantedStoreIds.add(Integer.valueOf(storeIds[i]));
		}
	}
	
	public static AllFriendGroupLogEntity valueOf(ResultSet rs) {
		try {
			String groupId = rs.getString("groupId");
			String groupName = rs.getString("groupName");
			String storeIdsStr = rs.getString("storeIds");
			AllFriendGroupLogEntity groupLogEntity = new AllFriendGroupLogEntity(groupId, groupName);
			if(storeIdsStr != null) {
				String[] storeIds = storeIdsStr.split(",");
				groupLogEntity.addStoreIds(storeIds);
			}
			return groupLogEntity;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("从数据库中还原AllFriendGroupLogEntity发生异常",e);
		}
	}
	public boolean isGranted(StoreEntity store) {
		Integer storeId = store.getId();
		return grantedStoreIds.contains(storeId);
	}
	
	public void addGrantStore(StoreEntity store) {
		grantedStoreIds.add(store.getId());
	}
	
	public Map<String,Object> toMap(){
		Map<String,Object> paramMap = Maps.newHashMap();
		paramMap.put("groupId", this.groupId);
		paramMap.put("groupName", this.groupName);
		paramMap.put("storeIds", Joiner.on(",").join(grantedStoreIds));
		return paramMap;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public TreeSet<Integer> getGrantedStoreIds() {
		return grantedStoreIds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((grantedStoreIds == null) ? 0 : grantedStoreIds.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AllFriendGroupLogEntity))
			return false;
		AllFriendGroupLogEntity other = (AllFriendGroupLogEntity) obj;
		if (grantedStoreIds == null) {
			if (other.grantedStoreIds != null)
				return false;
		} else if (!grantedStoreIds.equals(other.grantedStoreIds))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("AllFriendGroupLogEntity [groupId=%s, groupName=%s, grantedStoreIds=%s]", groupId,
				groupName, grantedStoreIds);
	}
	
	
	
}
