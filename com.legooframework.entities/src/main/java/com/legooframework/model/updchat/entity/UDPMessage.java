package com.legooframework.model.updchat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.imchat.entity.ChatMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UDPMessage {

    private String tag, fromDevicesId, toDevicesId, action, ruleId;
    private int len;
    private Map<String, Object> value;
    // 未读消息数量报文封装对象
    private List<Map<String, Object>> values;

    public void dropMsg() {
        this.tag = "dropmsg";
    }

    public boolean isDropMsg() {
        return StringUtils.equals(this.tag, "dropmsg");
    }

    public void mathcedKeywords() {
        this.value.put("matched", true);
    }

    public boolean isMathcerKeywords() {
        return MapUtils.getBoolean(this.value, "matched", false);
    }

    public void setMatchedKeys(List<String> keys) {
        this.value.put("keywords", keys);
    }

    @SuppressWarnings("unchecked")
    public List<String> getMatchedKeys() {
        return (List<String>) this.value.get("keywords");
    }

    public void setValue(Map<String, Object> value) {
        this.value = value;
    }

    public void setValues(List<Map<String, Object>> values) {
        this.values = values;
    }

    public ChatMessage getChatMessage() {
        return ChatMessage.fromUDP(value);
    }

    public void setToDevicesId(String toDevicesId) {
        this.toDevicesId = toDevicesId;
    }

    UDPMessage(String tag, String fromDevicesId, String toDevicesId, String action, String ruleId, int len,
               Map<String, Object> value) {
        this.tag = tag;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fromDevicesId), "fromDevicesId 不可以为空值...");
        this.fromDevicesId = fromDevicesId;
        this.toDevicesId = toDevicesId;
        this.action = Strings.nullToEmpty(action);
        this.ruleId = Strings.nullToEmpty(ruleId);
        this.value = value;
        this.len = len;
    }

    public UDPMessage(String tag, String fromDevicesId, String toDevicesId, String action, String ruleId, int len) {
        this.tag = tag;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fromDevicesId), "fromDevicesId 不可以为空值...");
        this.fromDevicesId = fromDevicesId;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toDevicesId), "toDevicesId 不可以为空值...");
        this.toDevicesId = toDevicesId;
        this.action = Strings.nullToEmpty(action);
        this.ruleId = Strings.nullToEmpty(ruleId);
        this.len = len;
    }

    UDPMessage(String tag, String fromDevicesId, String toDevicesId, String action, String ruleId,
               List<Map<String, Object>> values, int len) {
        this.tag = tag;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fromDevicesId), "fromDevicesId 不可以为空值...");
        this.fromDevicesId = fromDevicesId;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toDevicesId), "toDevicesId 不可以为空值...");
        this.toDevicesId = toDevicesId;
        this.action = Strings.nullToEmpty(action);
        this.ruleId = Strings.nullToEmpty(ruleId);
        this.values = CollectionUtils.isEmpty(values) ? null : values;
        this.len = len;
    }

    public Optional<List<Map<String, Object>>> getValues() {
        return Optional.ofNullable(values);
    }

    public boolean isHeartReqMsg() {
        return StringUtils.equals("heart", this.tag);
    }

    // 单条通讯录通讯报文
    public boolean isSingleContactMsg() {
        return StringUtils.equals("single_contact", this.tag);
    }

    // 通讯录上传完成事件
    public boolean isContactFinishMsg() {
        return StringUtils.equals("contact_finish", this.tag);
    }

    public boolean isHisMsgMsg() {
        return StringUtils.equals("hismsg", this.tag);
    }

    public boolean isDelMsgMsg() {
        return StringUtils.equals("delete_msg", this.tag);
    }

    public boolean isTxtMsg() {
        return 1 == MapUtils.getIntValue(this.value, "type", -1);
    }

    public String getContent() {
        return MapUtils.getString(this.value, "content", null);
    }

    public String getFromUser() {
        return MapUtils.getString(this.value, "fromUser", null);
    }

    public String getToUser() {
        return MapUtils.getString(this.value, "toUser", null);
    }

    public void setFromUserName(String name) {
        this.value.put("fromUserName", name);
    }

    public String getFromUserName() {
        return MapUtils.getString(this.value, "fromUserName", null);
    }

    public void setFromUserIcon(String icon) {
        this.value.put("fromUserIcon", icon);
    }

    public String getFromUserIcon() {
        return MapUtils.getString(this.value, "fromUserIcon", null);
    }

    public void setToUserName(String name) {
        this.value.put("toUserName", name);
    }

    public String getToUserIcon() {
        return MapUtils.getString(this.value, "toUserIcon", null);
    }

    public String getToUserName() {
        return MapUtils.getString(this.value, "toUserName", null);
    }

    public void setToUserIcon(String icon) {
        this.value.put("toUserIcon", icon);
    }

    public void setOwnerWx(String wexinId) {
        if (Strings.isNullOrEmpty(MapUtils.getString(this.value, "ownerWechat"))) {
            this.value.put("ownerWechat", wexinId);
        }
    }

    public boolean isLoginReqMsg() {
        return StringUtils.equals("login", this.tag);
    }

    public boolean isLoginAckMsg() {
        return StringUtils.equals("ack", this.tag) && Objects.equal("login", MapUtils.getString(value, "tag"));
    }

    public boolean isListenerDevice(String deviceId) {
        return StringUtils.equals(toDevicesId, deviceId);
    }

    public boolean isChatListenerAck() {
        return StringUtils.equals("ack", this.tag) && Objects.equal("login", MapUtils.getString(value, "tag"));
    }

    public boolean isLogOutAckMsg() {
        return StringUtils.equals("ack", this.tag) && Objects.equal("logout", MapUtils.getString(value, "tag"));
    }


    public String getMsgId() {
        String id = MapUtils.getString(this.value, "id");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "UPD推送消息ID为空，非法值.");
        return id;
    }

    public boolean isUnclaimMsgAckMsg() {
        return StringUtils.equals("unclaim_msg", this.tag);
    }

    public boolean isUnclaimMsgReqMsg() {
        return StringUtils.equals("unclaim_msg", this.tag);
    }

    public boolean isMsgAckMsg() {
        return StringUtils.equals("ack", this.tag) && Objects.equal("msg", MapUtils.getString(value, "tag"));
    }

    public boolean isMsgAckStausMsg() {
        return StringUtils.equals("ackmsg", this.tag);
    }

    public int getAckValue() {
        return MapUtils.getIntValue(this.value, "_ack");
    }

    public boolean isChatRecMsg() {
        return StringUtils.equals("msg", this.tag);
    }

    public boolean isChatReqMsg() {
        return StringUtils.equals("msg", this.tag);
    }

    // 领用应答包
    public boolean isClmAckMsg() {
        return StringUtils.equals("claim", this.tag);
    }

    public boolean isHeartAckMsg() {
        return StringUtils.equals("ack", this.tag) && Objects.equal("heart", MapUtils.getString(value, "tag"));
    }

    public boolean isAckMsgStatusOk() {
        return 1 == MapUtils.getIntValue(this.value, "_ack");
    }

    public boolean isReceiveMsg() {
        return isMsgAckMsg() || isUnclaimMsgAckMsg() || isChatRecMsg();
    }

    public UDPChatMessage getReceiveMsg() {
        Preconditions.checkState(isReceiveMsg() || isHisMsgMsg()||isDropMsg()||isDelMsgMsg(), "该消息不是推送消息，不支持该方法.");
        return new UDPChatMessage(this.value);
    }

    public String getTag() {
        return tag;
    }

    public Optional<Long> getCompanyId() {
        return Optional.ofNullable(MapUtils.getLong(this.value, "companyId", null));
    }

    public Optional<Long> getStoreId() {
        return Optional.ofNullable(MapUtils.getLong(this.value, "storeId", null));
    }

    public String getFromDevicesId() {
        return fromDevicesId;
    }

    public String getToDevicesId() {
        return toDevicesId;
    }

    public String getAction() {
        return action;
    }

    public String getRuleId() {
        return ruleId;
    }

    public int getLen() {
        return len;
    }

    public Map<String, Object> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tag", tag)
                .add("fromDevicesId", fromDevicesId)
                .add("toDevicesId", toDevicesId)
                .add("action", action)
                .add("ruleId", ruleId)
                .add("len", len)
                .add("value", value)
                .add("values", values)
                .omitNullValues()
                .toString();
    }
}
