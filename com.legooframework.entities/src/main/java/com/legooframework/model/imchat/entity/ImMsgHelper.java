package com.legooframework.model.imchat.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.legooframework.model.updchat.entity.UDPChatMessage;
import com.legooframework.model.updchat.entity.UDPMessage;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ImMsgHelper {
    private static final Logger logger = LoggerFactory.getLogger(ImMsgHelper.class);

    private static final String KEY_TYPE = "type";
    private static final String KEY_DATA = "data";

    private static String[] ACTION_REQ = new String[]{"0100", "0200"};
    // 0301 错误消息 0000 系统消息
    private static String[] ACTION_ACK = new String[]{"0101", "0201", "0000", "FFFF", "0301"};

    public static ImMessage decodeing(String jsonStr) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jsonStr), "非法的入参请求报文-空报文.");
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonStr);
        JsonObject jsonObject = element.getAsJsonObject();
        String type = jsonObject.get(KEY_TYPE).getAsString();
        Preconditions.checkArgument(ArrayUtils.contains(ACTION_REQ, type), "非法的请求type=%s值", type);
        // 聊天请求
        if(StringUtils.equals(ACTION_REQ[0], type)) {
            String data = jsonObject.get(KEY_DATA).getAsString();
            return new ImMessage(ACTION_REQ[0], ChatMessage.fromWEB(WebUtils.fromJsonByMap(data)));
        } else if(StringUtils.equals(ACTION_REQ[1], type)) {
            return new ImMessage(ACTION_REQ[1]);
        }
        throw new IllegalArgumentException(String.format("非法的请求 %s,不支持该报文协议。", jsonStr));
    }

    public static ImMessage encodingUnReadAck(List<Map<String, Object>> unreads) {
        return new ImMessage(ACTION_ACK[1], unreads);
    }

    public static ImMessage encodingAddFriendMsg(Long storeId, String userId, String nickname, String iconUrl) {
        return new ImMessage(ACTION_ACK[2], storeId, userId, nickname, iconUrl);
    }

    public static boolean isAddFirendMsg(ImMessage msg) {
        return StringUtils.equals(ACTION_ACK[2], msg.getAction());
    }

    public static ImMessage encodingSysMsg(String code, String message) {
        return new ImMessage(ACTION_ACK[2], code, message);
    }

    public static ImMessage encodingDropMsg() {
        return new ImMessage(ACTION_ACK[3]);
    }

    public static boolean isDropMsg(ImMessage message) {
        return StringUtils.equals(ACTION_ACK[3], message.getAction());
    }

    public static ImMessage encodingChatSignleRspMsg(ChatMessage chatMessage) {
        return new ImMessage(ACTION_ACK[0], chatMessage);
    }

    public static boolean isChatReqMsg(ImMessage message) {
        return StringUtils.equals(ACTION_REQ[0], message.getAction());
    }

    public static boolean isUnReadReqMsg(ImMessage message) {
        return StringUtils.equals(ACTION_REQ[1], message.getAction());
    }

    public static boolean isChatRspMsg(ImMessage message) {
        return StringUtils.equals(ACTION_ACK[0], message.getAction());
    }

    public static boolean isUnReadAckMsg(ImMessage message) {
        return StringUtils.equals(ACTION_ACK[1], message.getAction());
    }

    public static boolean isSysRspMsgMsg(ImMessage message) {
        return StringUtils.equals(ACTION_ACK[2], message.getAction());
    }

    public static String toJson(ImMessage message) {
        return WebUtils.toJson(message.toData());
    }

    public static Optional<String> fromUDPMessage4Monitor(UDPMessage udpMessage) {
        Map<String, Object> payload = null;
        if(udpMessage.isHisMsgMsg() || udpMessage.isDelMsgMsg()) {
            UDPChatMessage chatMessage = udpMessage.getReceiveMsg();
            Map<String, Object> data = Maps.newHashMap();
            data.put("msgId", chatMessage.getId());
            data.put("fromid", chatMessage.getFromUser());
            data.put("fromUserName", udpMessage.getFromUserName());
            data.put("fromidIcon", udpMessage.getFromUserIcon());
            data.put("cid", chatMessage.getId());
            data.put("toid", chatMessage.getToUser());
            data.put("toUserName", udpMessage.getToUserName());
            data.put("toidIcon", udpMessage.getToUserIcon());
            int issend = MapUtils.getInteger(udpMessage.getValue(), "isSend");

            if(0 == issend) {
                data.put("weixinId", chatMessage.getFromUser());
                data.put("mine", true);
            } else {
                data.put("weixinId", chatMessage.getToUser());
                data.put("mine", false);
            }
            
            data.put("storeId", udpMessage.getStoreId().orElse(null));
            data.put("companyId", udpMessage.getCompanyId().orElse(null));
            data.put(KEY_TYPE, chatMessage.getType());

            data.put("isMatched", udpMessage.isMathcerKeywords());
            if(udpMessage.isMathcerKeywords()) {
                data.put("keywords", Joiner.on(',').join(udpMessage.getMatchedKeys()));
            }

            data.put("chatTimeUnix", chatMessage.getCreateTime());
            data.put("chatTime", DateFormatUtils.format(new Date(chatMessage.getCreateTime()),
                    "yyyy-MM-dd HH:mm:ss"));
            if(chatMessage.isTxtMsg()) {
                data.put("content", chatMessage.getContent());
            } else if(chatMessage.isPicMsg() || chatMessage.isFceMsg()) {
                data.put("content", String.format("img[%s]", chatMessage.getContent()));
            } else if(chatMessage.isAioMsg()) {
                data.put("content", String.format("audio[%s]", chatMessage.getContent()));
            } else if(chatMessage.isHtmMsg()) {
                data.put("content", String.format("a(%s)", chatMessage.getContent()));
            } else {
                logger.warn(String.format("当前消息 %s 类型解析暂不支持监控解析", udpMessage));
                data.put("content", chatMessage.getContent());
            }
            if(udpMessage.isHisMsgMsg())
            	data.put("monitorType", 1);
            if(udpMessage.isDelMsgMsg())
            	data.put("monitorType", 2);
            payload = Maps.newHashMap();
            payload.put("type", "3001");
            payload.put("data", WebUtils.toJson(data));
            return Optional.of(WebUtils.toJson(payload));
        }else if(udpMessage.isDropMsg()){
        	UDPChatMessage chatMessage = udpMessage.getReceiveMsg();
            Map<String, Object> data = Maps.newHashMap();
            data.put("msgId", chatMessage.getId());
            data.put("fromid", chatMessage.getFromUser());
            data.put("fromUserName", udpMessage.getFromUserName());
            data.put("fromidIcon", udpMessage.getFromUserIcon());
            data.put("cid", chatMessage.getId());
            data.put("toid", udpMessage.getToUser());
            data.put("toUserName", udpMessage.getToUserName());
            data.put("toidIcon", udpMessage.getToUserIcon());
            int issend = MapUtils.getInteger(udpMessage.getValue(), "isSend");

            if(0 == issend) {
                data.put("weixinId", udpMessage.getFromUser());
                data.put("mine", true);
            } else {
                data.put("weixinId", udpMessage.getToUser());
                data.put("mine", false);
            }
            data.put("storeId", udpMessage.getStoreId().orElse(null));
            data.put("companyId", udpMessage.getCompanyId().orElse(null));
            data.put(KEY_TYPE, chatMessage.getType());

            data.put("isMatched", udpMessage.isMathcerKeywords());
            if(udpMessage.isMathcerKeywords()) {
                data.put("keywords", Joiner.on(',').join(udpMessage.getMatchedKeys()));
            }

            data.put("chatTimeUnix", chatMessage.getCreateTime());
            data.put("chatTime", DateFormatUtils.format(new Date(chatMessage.getCreateTime()),
                    "yyyy-MM-dd HH:mm:ss"));
            if(chatMessage.isTxtMsg()) {
                data.put("content", chatMessage.getContent());
            } else if(chatMessage.isPicMsg() || chatMessage.isFceMsg()) {
                data.put("content", String.format("img[%s]", chatMessage.getContent()));
            } else if(chatMessage.isAioMsg()) {
                data.put("content", String.format("audio[%s]", chatMessage.getContent()));
            } else if(chatMessage.isHtmMsg()) {
                data.put("content", String.format("a(%s)", chatMessage.getContent()));
            } else {
                logger.warn(String.format("当前消息 %s 类型解析暂不支持监控解析", udpMessage));
                data.put("content", chatMessage.getContent());
            }
            data.put("monitorType", 0);
            payload = Maps.newHashMap();
            payload.put("type", "3001");
            
            payload.put("data", WebUtils.toJson(data));

            return Optional.of(WebUtils.toJson(payload));
        } else {
            logger.warn(String.format("当前消息 %s 类型解析暂不支持监控解析，丢去处理", udpMessage));
        }
        return Optional.empty();
    }

}
