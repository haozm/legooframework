package com.legooframework.model.updchat.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.imchat.entity.ChatMessage;
import com.legooframework.model.core.utils.WebUtils;

import java.util.Map;

public class UDPMsgHelper {

    private String tag, fromDevicesId, toDevicesId, action, ruleId;
    private Map<String, Object> payload = Maps.newHashMap();

    private UDPMsgHelper(String tag, String action, String ruleId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(tag), "入参tag 不可以为空值...");
        this.tag = tag;
        this.action = Strings.nullToEmpty(action);
        this.ruleId = Strings.nullToEmpty(ruleId);
    }

    private UDPMsgHelper(String tag, String fromDevicesId, String toDevicesId, String action, String ruleId) {
        this.tag = tag;
        this.fromDevicesId = fromDevicesId;
        this.toDevicesId = toDevicesId;
        this.action = action;
        this.ruleId = ruleId;
    }

    // 登陆不需要指定  toDeviceId
    public static UDPMsgHelper login(LoginContext loginUser) {
        UDPMsgHelper builder = new UDPMsgHelper("login", String.format("EMP_%s", loginUser.getLoginId()),
                "www.csosm.com", "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "login");
        payload.put("ids", loginUser.getLoginId());
        builder.payload = payload;
        return builder;
    }

    public static UDPMsgHelper loginForChatHis(String fromDevicesId) {
        UDPMsgHelper builder = new UDPMsgHelper("login", "", "");
        builder.fromDevicesId = fromDevicesId;
        builder.toDevicesId = "T_FFFFFFFFFFF";
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "login");
        payload.put("ids", "9527");
        builder.payload = payload;
        return builder;
    }

    public static UDPMsgHelper ackOkMsgMessage(LoginContext loginUser, String msgId) {
        UDPMsgHelper builder = new UDPMsgHelper("ackmsg", String.format("EMP_%s", loginUser.getLoginId()),
                null, "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("_fromdevicesid", loginUser.getLoginId());
        payload.put("_msgid", msgId);
        payload.put("_ack", 1);
        payload.put("_ttl", 0);
        builder.payload = payload;
        return builder;
    }

    public static UDPMsgHelper unclaimmsg(LoginContext loginUser) {
        UDPMsgHelper builder = new UDPMsgHelper("unclaim_msg", String.format("EMP_%s", loginUser.getLoginId()),
                null,
                "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", null);
        payload.put("ids", loginUser.getLoginId().toString());
        builder.payload = payload;
        return builder;
    }

    public static UDPMsgHelper send(LoginContext loginUser) {
        return new UDPMsgHelper("msg", String.format("EMP_%s", loginUser.getLoginId()), null, "0", "1");
    }

    public UDPMsgHelper withChatMessage(ChatMessage chatMessage) {
        this.payload.putAll(chatMessage.toData());
        return this;
    }

    public static UDPMsgHelper logout(LoginContext loginUser) {
        UDPMsgHelper builder = new UDPMsgHelper("logout", String.format("EMP_%s", loginUser.getLoginId()), null
                , "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "logout");
        payload.put("ids", loginUser.getLoginId().toString());
        builder.payload = payload;
        return builder;
    }

    public static UDPMsgHelper heart(LoginContext loginUser) {
        UDPMsgHelper builder = new UDPMsgHelper("heart", String.format("EMP_%s", loginUser.getLoginId()),
                "www.csosm.com", "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "heart");
        payload.put("ids", loginUser.getLoginId().toString());
        builder.payload = payload;
        return builder;
    }

    public static UDPMsgHelper heart(String userId, String fromDevicesId, String toDevicesId) {
        UDPMsgHelper builder = new UDPMsgHelper("heart", "", "");
        builder.fromDevicesId = fromDevicesId;
        builder.toDevicesId = toDevicesId;
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "heart");
        payload.put("ids", userId);
        builder.payload = payload;
        return builder;
    }

    public UDPMessage toMessage() {
        return new UDPMessage(tag, fromDevicesId, toDevicesId, action, ruleId, WebUtils.toJson(this.payload).length(),
                this.payload);
    }

}
