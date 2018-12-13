package com.legooframework.model.imchat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.UUID;

public class ChatMessage {

    private final String msgId, fromUser, toUser, content, serviceId, fromChannel;
    private final boolean group;
    private final long timestamp;
    // 1 文本消息//3 图片//34 语音//43 视频//47 图片表情//49 网页信息
    private int contentType;

    private ChatMessage(String fromChannel, String msgId, String fromUser, String toUser, String content,
                        long timestamp, String serviceId, boolean group, int contentType) {
        this.msgId = msgId;
        this.fromChannel = fromChannel;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.serviceId = serviceId;
        this.group = group;
        this.timestamp = timestamp;
        this.contentType = contentType;
    }

    public static ChatMessage fromWEB(Map<String, String> data) {
        String content = MapUtils.getString(data, "content");
        int _content_type = 1;
        if (StringUtils.startsWith(content, "img[") && StringUtils.endsWith(content, "]")) {
            content = StringUtils.substring(content, 4, content.length() - 1);
            _content_type = 3;
        } else if (StringUtils.startsWith(content, "audio[") && StringUtils.endsWith(content, "]")) {
            content = StringUtils.substring(content, 6, content.length() - 1);
            _content_type = 34;
        } else if (StringUtils.startsWith(content, "video[") && StringUtils.endsWith(content, "]")) {
            content = StringUtils.substring(content, 6, content.length() - 1);
            _content_type = 43;
        } else if (StringUtils.startsWith(content, "a[") && StringUtils.endsWith(content, "]")) {
            content = StringUtils.substring(content, 6, content.length() - 1);
            _content_type = 47;
        }
        return new ChatMessage("WEB", MapUtils.getString(data, "msgid"),
                MapUtils.getString(data, "fromid"),
                MapUtils.getString(data, "toid"), content, System.currentTimeMillis(),
                null,
                StringUtils.equalsIgnoreCase("group", MapUtils.getString(data, "type")),
                _content_type);
    }

    // toUser=4213, fromUser=xiaojie_0710, createTime=1530236536000,
    // isSend=0, id=4207, type=1, content=继续的基督教, isGoup=false
    public static ChatMessage fromUDP(Map<String, Object> data) {
        return new ChatMessage("UDP", MapUtils.getString(data, "id"),
                MapUtils.getString(data, "fromUser"),
                MapUtils.getString(data, "toUser"),
                MapUtils.getString(data, "content"),
                MapUtils.getLongValue(data, "createTime"),
                MapUtils.getString(data, "id"),
                MapUtils.getBoolean(data, "isGoup"),
                MapUtils.getIntValue(data, "type"));
    }

    public Map<String, Object> toData() {
        Map<String, Object> data = Maps.newHashMap();
        if (isFromWeb()) {
            data.put("id", this.msgId);
            data.put("fromUser", this.fromUser);
            data.put("toUser", this.toUser);
            data.put("createTime", this.timestamp);
            data.put("isSend", 1);
            data.put("isGoup", this.isGroup());
            data.put("content", this.content);
            data.put("type", this.contentType);
        } else if (isFromUDP()) {
            data.put("msgId", this.msgId);
            data.put("fromid", this.fromUser);
            data.put("toid", this.toUser);
            data.put("timestamp", this.timestamp);
            data.put("cid", this.serviceId);
            data.put("type", this.isGroup() ? "group" : "friend");
            if (isFceMsg() || isPicMsg()) {
                data.put("content", String.format("img[%s]", this.content));
            } else if (isAioMsg()) {
                data.put("content", String.format("audio[%s]", this.content));
            } else if (isVioMsg()) {
                data.put("content", String.format("video[%s]", this.content));
            } else if (isHtmMsg() || StringUtils.startsWithAny(this.content, "http://", "https://")) {
                data.put("content", String.format("a(%s)[%s]", this.content, this.content));
            } else {
                data.put("content", this.content);
            }
            data.put("mine", false);
        }
        return data;
    }

    public boolean isTxtMsg() {
        return 1 == this.contentType;
    }

    public boolean isPicMsg() {
        return 3 == this.contentType;
    }

    public boolean isAioMsg() {
        return 34 == this.contentType;
    }

    public boolean isVioMsg() {
        return 43 == this.contentType;
    }

    public boolean isFceMsg() {
        return 47 == this.contentType;
    }

    public boolean isHtmMsg() {
        return 49 == this.contentType;
    }

    public boolean isFromWeb() {
        return StringUtils.equals("WEB", this.fromChannel);
    }

    public boolean isFromUDP() {
        return StringUtils.equals("UDP", this.fromChannel);
    }

    public boolean isGroup() {
        return group || StringUtils.equals("@chartRoom", this.toUser);
    }

    public String getMsgId() {
        return msgId;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public String getContent() {
        return content;
    }

    public String getServiceId() {
        return serviceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("msgId", msgId)
                .add("fromUser", fromUser)
                .add("toUser", toUser)
                .add("content", content)
                .add("serviceId", serviceId)
                .add("timestamp", timestamp)
                .add("contentType", contentType)
                .toString();
    }
}
