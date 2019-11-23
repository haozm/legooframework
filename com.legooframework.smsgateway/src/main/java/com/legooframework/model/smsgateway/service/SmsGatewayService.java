package com.legooframework.model.smsgateway.service;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.smsgateway.entity.*;
import com.legooframework.model.smsprovider.entity.SMSSettingEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SmsGatewayService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayService.class);

    /**
     * 时间监听 外部变量
     *
     * @param message msg
     */
    public void smsgatewayMessageHandler(Message<?> message) {
        MessageHeaderAccessor headerAccessor = MessageHeaderAccessor.getMutableAccessor(message);
        Object eventName = headerAccessor.getHeader("EventName");
        if (Objects.equal("sendMessage", eventName)) {
            final SendMessageAgg sendMessageAgg = (SendMessageAgg) message.getPayload();
            CompletableFuture.runAsync(() -> {
                LoginContextHolder.setAnonymousCtx();
                try {
                    OrgEntity company = getBean(OrgEntityAction.class).loadComById(sendMessageAgg.getCompanyId());
                    StoEntity store = getBean(StoEntityAction.class).loadById(sendMessageAgg.getStoreId());
                    this.batchSaveMessage(company, store, sendMessageAgg.getBuilders(), null, null);
                } finally {
                    LoginContextHolder.clear();
                }
            });
        }
        if (logger.isDebugEnabled())
            logger.debug(message.toString());
    }

    public boolean batchSaveMessage(OrgEntity company, StoEntity store, List<SendMessageBuilder> msgBuilder,
                                    String msgTemplate, UserAuthorEntity user) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(msgBuilder));
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSaveMessage(store=%d,sendMsgTemplates'size=%d,user=%s) start", store.getId(),
                    msgBuilder.size(), user == null ? null : user.getId()));

        final int size = msgBuilder.size();
        List<List<SendMessageBuilder>> partition = null;
        if (size <= 30) {
            partition = Lists.partition(msgBuilder, 30);
        } else if (size <= 100) {
            partition = Lists.partition(msgBuilder, 30);
        } else if (size <= 1000) {
            partition = Lists.partition(msgBuilder, 300);
        } else if (size <= 3000) {
            partition = Lists.partition(msgBuilder, 500);
        } else if (size <= 5000) {
            partition = Lists.partition(msgBuilder, 800);
        } else {
            partition = Lists.partition(msgBuilder, 1000);
        }
        List<SendMsgStateEntity> instances = Lists.newArrayList();
        List<Throwable> errHolder = Lists.newArrayList();
        CompletableFuture.allOf(partition.stream()
                .map(list -> CompletableFuture.supplyAsync(() -> initMessage(list, msgTemplate))
                        .thenAccept(msgs -> msgs.forEach(msg -> instances.add(SendMsgStateEntity.createInstance(store, msg)))))
                .toArray(CompletableFuture[]::new))
                .whenComplete((v, th) -> {
                    if (null != th) {
                        logger.error("CompletableFuture.supplyAsync has  error ", th);
                        errHolder.add(th);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug(String.format("initMessage() size %d finished", msgBuilder.size()));
                    }
                }).join();
        if (CollectionUtils.isNotEmpty(errHolder))
            throw new RuntimeException(errHolder.get(0));

        List<SendMsgStateEntity> sms_list = instances.stream().filter(SendMsgStateEntity::isEnbaled)
                .filter(SendMsgStateEntity::isSMSMsg).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(sms_list)) {
            SMSSettingEntity smsSetting = smsSettingEntityAction.loadByStore(company, store);
            sms_list.forEach(sms -> sms.getSms().addPrefix(smsSetting.getSmsPrefix()));
        }
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        boolean flag = false;
        TransactionStatus tx = startTx(null);
        try {
            String batchNo = sendMsgStateEntityAction.batch4MsgInit(store, instances);
            SendMode sendMode = msgBuilder.size() == 1 ? SendMode.ManualSingle : SendMode.ManualBatch;
            msgTransportBatchEntityAction.insert(store, batchNo, sendMode, instances, user);
            commitTx(tx);
            flag = true;
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("batchSaveMessage(store=%d,sendMsgTemplates'size=%d) has error",
                        store.getId(), msgBuilder.size()), e);
            rollbackTx(tx);
        } finally {
            LoginContextHolder.clear();
        }
        return flag;
    }

    /**
     * 处理消息米板 转化为 发送消息
     *
     * @param msgBuilder  IOXX
     * @param msgTemplate XXOO
     * @return OXOX
     */
    private List<MsgEntity> initMessage(List<SendMessageBuilder> msgBuilder, String msgTemplate) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            List<MsgEntity> instances = Lists.newArrayList();
            for (SendMessageBuilder $temp : msgBuilder) {
                String _template = $temp.getCtxTemplate().orElse(msgTemplate);
                // NO template
                if (Strings.isNullOrEmpty(_template)) {
                    $temp.setError("未发送信息模版");
                    continue;
                }
                // 无替换内容的模版
                if (!StringUtils.containsAny(_template, "{", "}")) {
                    $temp.setContext(_template);
                    continue;
                }

                Map<String, Object> replace_map = Maps.newHashMap();
                $temp.getReplaceMap().ifPresent(replace_map::putAll);
                if ($temp.hasMemberId()) {
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
                    replace_map.putAll(memberAgg.toReplaceMap());
                }

                try {
                    $temp.setContext(replaceTemplate(_template, replace_map));
                } catch (Exception e) {
                    logger.error(String.format("fmtMsgTemplate(%s) has error...", _template), e);
                    $temp.setError(String.format("格式化模板异常%s", _template));
                }
            }
            msgBuilder.forEach(msg -> instances.addAll(MsgEntity.createSMSMsg(msg)));
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
