package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.TemplateReplaceException;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsgateway.entity.SendMessageTemplate;
import com.legooframework.model.smsgateway.entity.SendMsg4InitEntity;
import com.legooframework.model.smsgateway.entity.SendMsg4InitEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.StringSubstitutor;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.transaction.TransactionStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmsGatewayService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayService.class);

    public boolean batchSaveMessage(StoEntity store, Collection<SendMessageTemplate> sendMsgTemplates, String msgTemplate,
                                    UserAuthorEntity user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSaveMessage(store=%d,sendMsgTemplates'size=%d,user=%s) start", store.getId(),
                    sendMsgTemplates.size(), user));
        boolean flag = false;
        TransactionStatus tx = startTx(null);
        try {
            List<SendMsg4InitEntity> instances = Lists.newArrayList();
            for (SendMessageTemplate $temp : sendMsgTemplates) {
                MemberAgg memberAgg = null;
                try {
                    memberAgg = getBean(CovariantService.class).loadMemberAgg($temp.getMemberId());
                } catch (Exception e) {
                    logger.error(String.format("loadMemberAgg(%d) has error...", $temp.getMemberId()), e);
                    $temp.setError(String.format("获取ID=%d的用户失败", $temp.getMemberId()));
                    continue;
                }
                Preconditions.checkNotNull(memberAgg);
                $temp.setMemberInfo(memberAgg.getMember().getPhone(), memberAgg.getMember().getName());
                memberAgg.getWxUser().ifPresent(wx -> $temp.setWeixinInfo(wx.getId(), wx.getDevicesId()));
                String _template = $temp.getCtxTemplate().orElse(msgTemplate);
                try {
                    $temp.setContext(replaceTemplate(_template, memberAgg.toReplaceMap()));
                } catch (Exception e) {
                    logger.error(String.format("fmtMsgTemplate(%s) has error...", _template), e);
                    $temp.setError(String.format("格式化模板异常%s", _template));
                    continue;
                }
                SMSEntity.createSMSMsg($temp).forEach(sms ->
                        instances.add(SendMsg4InitEntity.createInstance(store, sms, $temp.getBusinessType())));
            }
            List<SendMsg4InitEntity> sms_list = instances.stream().filter(SendMsg4InitEntity::isEnbaled)
                    .filter(SendMsg4InitEntity::isSMSMsg).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sms_list)) {
                // 追加前缀与后坠
            }
            String batchNo = String.format("%d-%d-%s", store.getCompanyId(), store.getId(), LocalDateTime.now().toString("yyyyMMddHHmmss"));
            getBean(SendMsg4InitEntityAction.class).batchInsert(store, batchNo, instances);
            commitTx(tx);
            flag = true;
            Message<String> msg_request = MessageBuilder.withPayload(batchNo)
                    .setHeader("user", user)
                    .setHeader("action", "deduction")
                    .build();
            getMessagingTemplate().send(CHANNEL_SMS_BILLING, msg_request);
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("batchSaveMessage(store=%d,sendMsgTemplates'size=%d) has error",
                        store.getId(), sendMsgTemplates.size()), e);
            rollbackTx(tx);
        }
        return flag;
    }

    private String replaceTemplate(String content, Map<String, Object> params) throws TemplateReplaceException {
        if (MapUtils.isEmpty(params) || Strings.isNullOrEmpty(content)) return content;
        try {
            StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
            return substitutor.replace(content);
        } catch (Exception e) {
            throw new TemplateReplaceException(String.format("模板 %s 替换发送异常...%s", content, params), e);
        }
    }

}
