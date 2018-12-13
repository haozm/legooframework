package com.legooframework.model.wechat.event;

import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.wechat.entity.WechatFriendEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WechatModuleEvent extends LegooEvent {

    WechatModuleEvent(String source, String eventName) {
        super(source, "wechat", eventName);
    }

    WechatModuleEvent(String eventName) {
        super("wechatBundle", eventName);
    }

    public void setAccountId(Long wechatId) {
        super.putPayload("accountId", wechatId);
    }

    public Optional<Long> getAccountId() {
        return super.getValue("accountId", Long.class);
    }

    public void setUserName(String userName) {
        super.putPayload("userName", userName);
    }

    public String getUserName() {
        return super.getString("userName");
    }

    public void setOperation(String operation) {
        super.putPayload("operation", operation);
    }

    public String getOperation() {
        return super.getString("operation");
    }

    public void setDeviceId(String deviceId) {
        super.putPayload("deviceId", deviceId);
    }

    public String getDeviceId() {
        return super.getString("deviceId");
    }

    public void setWechatId(String wechatId) {
        super.putPayload("wechatId", wechatId);
    }

    public String getWechatId() {
        return super.getString("wechatId");
    }

    public void setFriendId(String friendId) {
        super.putPayload("friendId", friendId);
    }

    public String getFriendId() {
        return super.getString("friendId");
    }

    public void setWechatFriend(WechatFriendEntity friend) {
        super.putPayload("friend", friend);
    }

    public Optional<WechatFriendEntity> getWechatFriend() {
        return (Optional<WechatFriendEntity>) super.getValue("friend", WechatFriendEntity.class);
    }

    public void setStoreId(Long storeId) {
        super.putPayload("storeId", storeId);
    }

    public Optional<Long> getStoreId() {
        return super.getValue("storeId", Long.class);
    }

    public void setAccountIds(Collection<Long> accountIds) {
        super.putPayload("accountIds", accountIds);
    }

    public Optional<Collection> getAccountIds() {
        return super.getValue("accountIds", Collection.class);
    }

    public void setSyncWechatAccounts(List<Map<String, Object>> accounts) {
        super.putPayload("wechatAccounts", accounts);
    }

    public Optional<List> getSyncWechatAccounts() {
        return super.getValue("wechatAccounts", List.class);
    }

}
