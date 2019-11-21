package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SmsGatewayService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayService.class);

    public boolean batchSaveMessage(StoEntity store, List<SendMessageTemplate> sendMsgTemplates, String msgTemplate,
                                    UserAuthorEntity user) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(sendMsgTemplates));
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSaveMessage(store=%d,sendMsgTemplates'size=%d,user=%s) start", store.getId(),
                    sendMsgTemplates.size(), user));

        final int size = sendMsgTemplates.size();
        List<List<SendMessageTemplate>> partition = null;
        if (size <= 30) {
            partition = Lists.partition(sendMsgTemplates, 30);
        } else if (size <= 100) {
            partition = Lists.partition(sendMsgTemplates, 30);
        } else if (size <= 1000) {
            partition = Lists.partition(sendMsgTemplates, 300);
        } else if (size <= 3000) {
            partition = Lists.partition(sendMsgTemplates, 500);
        } else if (size <= 5000) {
            partition = Lists.partition(sendMsgTemplates, 800);
        } else {
            partition = Lists.partition(sendMsgTemplates, 1000);
        }
        List<SendMsg4InitEntity> instances = Lists.newArrayList();
        List<Throwable> errHolder = Lists.newArrayList();
        CompletableFuture.allOf(partition.stream()
                .map(list -> CompletableFuture.supplyAsync(() -> initMessage(list, msgTemplate))
                        .thenAccept(msgs -> msgs.forEach(msg -> instances.add(SendMsg4InitEntity.createInstance(store, msg)))))
                .toArray(CompletableFuture[]::new))
                .whenComplete((v, th) -> {
                    if (null != th) {
                        logger.error("CompletableFuture.supplyAsync has  error ", th);
                        errHolder.add(th);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug(String.format("initMessage() size %d finished", sendMsgTemplates.size()));
                    }
                }).join();
        if (CollectionUtils.isNotEmpty(errHolder))
            throw new RuntimeException(errHolder.get(0));

        List<SendMsg4InitEntity> sms_list = instances.stream().filter(SendMsg4InitEntity::isEnbaled)
                .filter(SendMsg4InitEntity::isSMSMsg).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(sms_list)) {
            // 追加前缀与后坠
        }
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        boolean flag = false;
        TransactionStatus tx = startTx(null);
        String batchNo = null;
        try {
            batchNo = getBean(SendMsg4InitEntityAction.class).batchInsert(store, instances);
            commitTx(tx);
            flag = true;
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("batchSaveMessage(store=%d,sendMsgTemplates'size=%d) has error",
                        store.getId(), sendMsgTemplates.size()), e);
            rollbackTx(tx);
        } finally {
            LoginContextHolder.clear();
        }
        return flag;
    }

    /**
     * 处理消息米板 转化为 发送消息
     *
     * @param sendMsgTemplates IOXX
     * @param msgTemplate      XXOO
     * @return OXOX
     */
    private List<SMSEntity> initMessage(List<SendMessageTemplate> sendMsgTemplates, String msgTemplate) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            List<SMSEntity> instances = Lists.newArrayList();
            for (SendMessageTemplate $temp : sendMsgTemplates) {
                MemberAgg memberAgg;
                try {
                    memberAgg = covariantService.loadMemberAgg($temp.getMemberId());
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
                }
            }
            sendMsgTemplates.forEach(msg -> instances.addAll(SMSEntity.createSMSMsg(msg)));
            return instances;
        } finally {
            LoginContextHolder.clear();
        }
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

    private CovariantService covariantService;

    public void setCovariantService(CovariantService covariantService) {
        this.covariantService = covariantService;
    }
}
