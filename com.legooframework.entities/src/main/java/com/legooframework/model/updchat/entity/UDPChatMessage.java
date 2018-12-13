package com.legooframework.model.updchat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class UDPChatMessage {

    private String id, fromUser, toUser, content;
    private int type;
    private boolean isGroup;
    private long createTime;
    private int isSend;

    public boolean isToChatRoomMsg() {
        return StringUtils.equals("@chartRoom", toUser);
    }

    public UDPChatMessage(Map<String, ? extends Object> data) {
        try {
            this.id = MapUtils.getString(data, "id");
            this.fromUser = MapUtils.getString(data, "fromuser");
            this.toUser = MapUtils.getString(data, "touser");
            this.content = MapUtils.getString(data, "content");
            this.type = MapUtils.getIntValue(data, "type");
            this.isGroup = MapUtils.getBoolean(data, "isGoup");
            this.createTime = MapUtils.getLongValue(data, "createTime");
            this.isSend = MapUtils.getIntValue(data, "isSend");
        } catch (Exception e) {
            throw new IllegalArgumentException("构建UDPChatMessage 发生异常，错误的入参数据.", e);
        }
    }

    //1 文本消息//3 图片//34 语音//43 视频//47 图片表情//49 网页信息
    public boolean isTxtMsg() {
        return 1 == this.type;
    }

    public boolean isPicMsg() {
        return 3 == this.type;
    }

    public boolean isAioMsg() {
        return 34 == this.type;
    }

    public boolean isVioMsg() {
        return 43 == this.type;
    }

    public boolean isFceMsg() {
        return 47 == this.type;
    }

    public boolean isHtmMsg() {
        return 49 == this.type;
    }

    UDPChatMessage(String id, String fromUser, String toUser, String content, int type) {
        this.id = id;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.type = type;
        this.isGroup = false;
        this.createTime = System.currentTimeMillis();
        this.isSend = 1;
    }

    public Map<String, Object> toData() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("id", id);
        data.put("fromUser", fromUser);
        data.put("toUser", toUser);
        data.put("content", content);
        data.put("type", type);
        data.put("isGroup", isGroup);
        data.put("createTime", createTime);
        data.put("isSend", isSend);
        return data;
    }

    public String getId() {
        return id;
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

    public int getType() {
        return type;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getIsSend() {
        return isSend;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("fromUser", fromUser)
                .add("toUser", toUser)
                .add("content", content)
                .add("type", type)
                .add("isGroup", isGroup)
                .add("createTime", createTime)
                .add("isSend", isSend)
                .toString();
    }
}
