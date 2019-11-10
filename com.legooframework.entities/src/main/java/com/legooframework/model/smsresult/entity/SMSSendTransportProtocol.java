package com.legooframework.model.smsresult.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsgateway.entity.SendStatus;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 短信发送  协议  解析包
 */
public class SMSSendTransportProtocol {

    private static final Logger logger = LoggerFactory.getLogger(SMSSendTransportProtocol.class);

    public static String encoding4Flat(String smsId, Integer companyId, Integer storeId, SMSChannel channel,
                                       SendStatus sendStatus, String phoneNo, int wordCount, int smsSum, String content) {
        // id|cId|sId|channel|status|mobile|count|sum|encoding|content
        Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "待发送的短信内容不可以为空");
        Preconditions.checkNotNull(channel, "待发送的短信通道不可以为空...");
        Preconditions.checkNotNull(sendStatus, "待发送的短信发送模式不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(phoneNo), "待发送的短信的手机号码不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(smsId), "待发送的短信的短信ID...");
        StringJoiner joiner = new StringJoiner("|");
        String payload = joiner.add(String.valueOf(companyId == null ? -1 : companyId))
                .add(String.valueOf(storeId == null ? -1 : storeId))
                .add(String.valueOf(channel.getChannel()))
                .add(String.valueOf(sendStatus.getStatus()))
                .add(phoneNo)
                .add(String.valueOf(wordCount))
                .add(String.valueOf(smsSum)).add("1")
                .add(WebUtils.encodeUrl(content)).toString();
        if (logger.isTraceEnabled())
            logger.trace(String.format("encoding4Flat(...) res is %s", payload));
        return payload;
    }

    /**
     * 解析报文
     *
     * @param payload 协议就是一切
     * @return 我的网址
     */
    public static SMSResultEntity decodingByFlat(String[] payload) {
        Preconditions.checkState(payload.length == 10, "报文格式异常,数据缺失");
        try {
            String smsId = payload[0];
            int companyId = Integer.parseInt(payload[1]);
            int storeId = Integer.parseInt(payload[2]);
            int channel = Integer.parseInt(payload[3]);
            int status = Integer.parseInt(payload[4]);
            Preconditions.checkState(SendStatus.SMS4Sending.getStatus() == status, "非法的短信发送状态:%s", status);
            String mobile = payload[5];
            int count = Integer.parseInt(payload[6]);
            int sum = Integer.parseInt(payload[7]);
            boolean encoding = StringUtils.equals("1", payload[8]);
            String content = encoding ? WebUtils.decodeUrl(payload[9]) : payload[9];
            SMSEntity sendSMS = SMSEntity.create4Sending(smsId, content, mobile, count, sum);
            return new SMSResultEntity(companyId, storeId, sendSMS, channel, SendStatus.SendedGateWay.getStatus(),
                    RandomUtils.nextLong(1L, 9999999999L));
        } catch (Exception e) {
            logger.error(String.format("decodingByFlat(%s) has exception", Arrays.toString(payload)), e);
            throw new RuntimeException("报文解析异常", e);
        }
    }
}
