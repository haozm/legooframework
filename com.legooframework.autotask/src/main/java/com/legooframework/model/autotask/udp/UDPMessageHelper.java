package com.legooframework.model.autotask.udp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.utils.WebUtils;

import java.util.Map;

public class UDPMessageHelper {

    private String tag, fromDevicesId, toDevicesId, action, ruleId;
    private Map<String, Object> payload = Maps.newHashMap();

    private UDPMessageHelper(String tag, String action, String ruleId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(tag), "入参tag 不可以为空值...");
        this.tag = tag;
        this.action = Strings.nullToEmpty(action);
        this.ruleId = Strings.nullToEmpty(ruleId);
    }

    private UDPMessageHelper(String tag, String fromDevicesId, String toDevicesId, String action, String ruleId) {
        this.tag = tag;
        this.fromDevicesId = fromDevicesId;
        this.toDevicesId = toDevicesId;
        this.action = action;
        this.ruleId = ruleId;
    }

    public static UDPMessageHelper login(String fromDevicesId) {
        UDPMessageHelper builder = new UDPMessageHelper("login", "", "");
        builder.fromDevicesId = fromDevicesId;
        builder.toDevicesId = "T_FFFFFFFFFFF";
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "login");
        payload.put("ids", "9527");
        builder.payload = payload;
        return builder;
    }

    public static UDPMessageHelper ackOkMsgMessage(String msgId) {
        UDPMessageHelper builder = new UDPMessageHelper("ackmsg", String.format("EMP_%s", 1234), null, "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("_fromdevicesid", 1234);
        payload.put("_msgid", msgId);
        payload.put("_ack", 1);
        payload.put("_ttl", 0);
        builder.payload = payload;
        return builder;
    }

    public static UDPMessageHelper unclaimmsg() {
        UDPMessageHelper builder = new UDPMessageHelper("unclaim_msg", String.format("EMP_%s", 1234), null, "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", null);
        payload.put("ids", "1234");
        builder.payload = payload;
        return builder;
    }

    public static UDPMessageHelper send() {
        return new UDPMessageHelper("msg", String.format("EMP_%s", 1234), null, "0", "1");
    }


    public static UDPMessageHelper logout() {
        UDPMessageHelper builder = new UDPMessageHelper("logout", String.format("EMP_%s", 1234), null, "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "logout");
        payload.put("ids", "1234");
        builder.payload = payload;
        return builder;
    }

    public static UDPMessageHelper heart() {
        UDPMessageHelper builder = new UDPMessageHelper("heart", String.format("EMP_%s", 1234), "T_FFFFFFFFFFF", "", "");
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("tag", "heart");
        payload.put("ids", "1234");
        builder.payload = payload;
        return builder;
    }

    public static UDPMessageHelper heart(String userId, String fromDevicesId, String toDevicesId) {
        UDPMessageHelper builder = new UDPMessageHelper("heart", "", "");
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
