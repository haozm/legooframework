package com.csosm.module.webchat.group;

import java.util.Collection;
import java.util.List;

import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.webchat.group.WeixinGroupEntity.Type;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class WeixinGroupFactory {

	private WeixinGroupFactory() {
		throw new AssertionError();
	}


	public static WeixinGroupEntity getAllFriendGroup(StoreEntity store) {
		Integer storeId = store.getId();
		Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店[%s]无对应的公司信息", storeId);
		Integer companyId = store.getCompanyId().get();
		return createAllFriendGroup(storeId,companyId);
	}
	
	private static WeixinGroupEntity createAllFriendGroup(Integer companyId,Integer storeId) {
		return new WeixinGroupEntity(Type.ALL_FRIEND_GROUP.getId(), Type.ALL_FRIEND_GROUP.getName(), storeId, companyId,
				Type.ALL_FRIEND_GROUP.getType(),0);
	}
	
	public static WeixinGroupEntity createEmployeeGroup(StoreEntity store) {
		Integer storeId = store.getId();
		Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店[%s]无对应的公司信息", storeId);
		Integer companyId = store.getCompanyId().get();
		return createEmployeeGroup(companyId, storeId);
	}
	
	private static WeixinGroupEntity createEmployeeGroup(Integer companyId,Integer storeId) {
		return new WeixinGroupEntity(Type.EMPLOYEE_GROUP.getId(), Type.EMPLOYEE_GROUP.getName(), storeId, companyId,
				Type.EMPLOYEE_GROUP.getType(),0);
	}
	
	public static WeixinGroupEntity getAddNewFriendGroup(StoreEntity store) {
		Integer storeId = store.getId();
		Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店[%s]无对应的公司信息", storeId);
		Integer companyId = store.getCompanyId().get();
		return new WeixinGroupEntity(Type.NEW_FRIEND_GROUP.getId(), Type.NEW_FRIEND_GROUP.getName(), storeId,
				companyId, Type.NEW_FRIEND_GROUP.getType(),0);
	}
	
	
	public static boolean isAllFriendGroup(String id) {
		return Type.ALL_FRIEND_GROUP == Type.of(id);
	}

	public static boolean isEmployeeGroup(String id) {
		return Type.EMPLOYEE_GROUP == Type.of(id);
	}

	public static boolean isNewFriendGroup(String id) {
		return Type.NEW_FRIEND_GROUP == Type.of(id);
	}
	
	public static boolean isLabelGroup(String id) {
		return id.indexOf("_label_") != -1;
	}
	
	public static boolean isCommonGroup(String id) {
		return !(isAllFriendGroup(id) || isNewFriendGroup(id) || isLabelGroup(id));
	}
	
	public static boolean hasAllFriendGroup(Collection<String> groupIds) {
		return groupIds.contains(Type.ALL_FRIEND_GROUP.getId());
	}
	
	public static boolean hasNewFriendGroup(Collection<String> groupIds) {
		return groupIds.contains(Type.NEW_FRIEND_GROUP.getId());
	}
	
	public static boolean hasLabelGroup(Collection<String> groupIds) {
		return !getLabelGroupIds(groupIds).isEmpty();
	}
	
	public static List<String> getLabelGroupIds(Collection<String> groupIds){
		List<String> labelGroupIds = Lists.newArrayList();
		for(String groupId : groupIds) {
			if(isLabelGroup(groupId)) labelGroupIds.add(groupId);
		}
		return labelGroupIds;
	}
	
	public static boolean hasCommonGroup(Collection<String> groupIds) {
		return !getCommonGroupIds(groupIds).isEmpty();
	}
	
	public static List<String> getCommonGroupIds(Collection<String> groupIds){
		List<String> commonGroupIds = Lists.newArrayList();
		for(String groupId : groupIds) {
			boolean isCommonGroup = (!isAllFriendGroup(groupId)&&!isNewFriendGroup(groupId)&&!isLabelGroup(groupId));
			if(isCommonGroup) commonGroupIds.add(groupId);
		}
		return commonGroupIds;
	}
	
	public static String getDeviceId(String labelGroupId) {
		return labelGroupId.substring(0, labelGroupId.lastIndexOf("_label_"));
	}

	public static String getLabelId(String labelGroupId) {
		return labelGroupId.substring(labelGroupId.lastIndexOf("_label_")+"_label_".length());
	}
	
}
