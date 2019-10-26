package com.csosm.module.webchat.group;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GroupParamsHanler {

    private Map<String, Object> datas;

    private LoginUserContext userContext;

    private static Gson gson = new Gson();

    private static Type type = new TypeToken<Map<String, String>>() {
    }.getType();

    public GroupParamsHanler(Map<String, Object> datas, LoginUserContext userContext) {
        this.datas = datas;
        this.userContext = userContext;
    }

    public OrganizationEntity getExistCompany() {
        Preconditions.checkState(userContext.getCompany().isPresent(), "当前登录用户无公司ID");
        return userContext.getCompany().get();
    }

    public String getExistGroupId() {
        Preconditions.checkArgument(datas.containsKey("groupId"), "请求参数必须传入groupId");
        return MapUtils.getString(datas, "groupId");
    }

    public List<String> getExistGroupIds() {
        Preconditions.checkArgument(datas.containsKey("groupIds"), "请求参数必须传入groupId");
        String groupIdsStr = MapUtils.getString(datas, "groupIds");
        String[] groupIds = groupIdsStr.split(",");
        return Lists.newArrayList(groupIds);
    }

    public String getExistGroupName() {
        Preconditions.checkArgument(datas.containsKey("groupName"), "请求参数必须传入groupName");
        return MapUtils.getString(datas, "groupName");
    }

    public String getFriendId() {
        Preconditions.checkArgument(datas.containsKey("friendId"), "请求参数必须传入friendId");
        String friendId = MapUtils.getString(datas, "friendId");
        return friendId;
    }

    public List<String> getFriendIds() {
        Preconditions.checkArgument(datas.containsKey("friendIds"), "请求参数必须传入friendIds");
        String friendIds = MapUtils.getString(datas, "friendIds");
        String[] friends = friendIds.split(",");
        return Lists.newArrayList(friends);
    }

    public List<Map<String, Object>> getGroups() {
        Preconditions.checkArgument(datas.containsKey("groups"), "请求参数必须传入groups");
        String groups = MapUtils.getString(datas, "groups");
        Map<String, String> groups_map = gson.fromJson(groups, type);
        List<Map<String, Object>> maps = Lists.newArrayList();
        for (Map.Entry<String, String> param : groups_map.entrySet()) {
            String groupId = param.getKey();
            String[] guideIds = StringUtils.split(param.getValue(), ",");
            Map<String, Object> map = Maps.newHashMap();
            map.put("groupId", groupId);
            map.put("guideIds", guideIds);
            maps.add(map);
        }
        return maps;
    }


    public StoreEntity getExistStore() {
        StoreEntity store = null;
        if (datas == null || datas.isEmpty() || !datas.containsKey("storeId")) {
            Preconditions.checkState(userContext.getStore().isPresent(), "当前登录用户无门店");
            store = userContext.getStore().get();
        }
        Preconditions.checkNotNull(store, "当前登录用户无门店");
        return store;
    }

    public Object getUserId() {
        return userContext.getUserId();
    }

}
