package com.legooframework.model.imchat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

public class ImMessage {

    private String action;
    private ChatMessage chatMsg;

    private static String KEY_CID = "cid", KEY_TYPE = "type", KEY_MSGID = "msgId",
            KEY_FROMID = "fromid", KEY_TOID = "toid", KEY_CONTENT = "content",
            KEY_CODE = "code", KEY_MESSAGE = "msg", KEY_MINE = "mine", KEY_TIMESTAMP = "timestamp";
    private Map<String, Object> data = Maps.newHashMap();
    private List<Map<String, Object>> unreads;

    // 请求消息构造函数
    ImMessage(String action, ChatMessage chatMsg) {
        this.action = action;
        this.chatMsg = chatMsg;
        this.data.putAll(chatMsg.toData());
    }

    ImMessage(String action, String code, String message) {
        this.action = action;
        this.data.put("code", code);
        this.data.put("msg", message);
    }

    // 新增好友消息
    ImMessage(String action, Long storeId, String userId, String nickname, String iconUrl) {
        this.action = action;
        this.data.put("storeId", storeId);
        this.data.put("userid", userId);
        this.data.put("username", nickname);
        this.data.put("avatar", iconUrl);
    }

    ImMessage(String action) {
        this.action = action;
    }

    ImMessage(String action, List<Map<String, Object>> unreads) {
        this.action = action;
        if(CollectionUtils.isNotEmpty(unreads))
            this.unreads = Lists.newArrayList(unreads);
    }

    public Long getStoreId() {
        return MapUtils.getLong(this.data, "storeId");
    }

    public ChatMessage getChatMsg() {
        return chatMsg;
    }

    public String getMsgid() {
        return MapUtils.getString(this.data, KEY_MSGID);
    }

    Map<String, Object> toData() {
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("type", this.action);
        if(ImMsgHelper.isChatRspMsg(this) || ImMsgHelper.isSysRspMsgMsg(this) || ImMsgHelper.isAddFirendMsg(this)) {
            payload.put("data", WebUtils.toJson(this.data));
        } else if(ImMsgHelper.isUnReadAckMsg(this)) {
            payload.put("data", WebUtils.toJson(unreads));
        }
        return payload;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", action)
                .add("data", data)
                .toString();
    }
}
