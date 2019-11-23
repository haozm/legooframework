package com.legooframework.model.reactor.entity;

import com.legooframework.model.covariant.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.Optional;

public class RetailFactAggItemProcessor implements ItemProcessor<RetailFactEntity, RetailFactAgg> {

    private static final Logger logger = LoggerFactory.getLogger(RetailFactAggItemProcessor.class);

    public RetailFactAggItemProcessor(StoEntityAction storeAction, TemplateEntityAction templateAction,
                                      SmsBalanceEntityAction smsBalanceAction, EmpEntityAction employeeAction,
                                      SendSmsEntityAction sendSmsEntityAction,
                                      MessagingTemplate messagingTemplate) {
        this.storeAction = storeAction;
        this.templateAction = templateAction;
        this.smsBalanceAction = smsBalanceAction;
        this.employeeAction = employeeAction;
        this.sendSmsEntityAction = sendSmsEntityAction;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public RetailFactAgg process(RetailFactEntity item) throws Exception {
        StoEntity store = storeAction.loadById(item.getStoreId());
        String smsPrefix = sendSmsEntityAction.getSmsPrefix(store);
        Optional<TemplateEntity> tempate =
                templateAction.findByStoreWithClassifies(store, TemplateEntity.CLASSIFIES_RIGHTS_AND_INTERESTS);
        if (!tempate.isPresent()) return new RetailFactAgg(item, store);
        Map<String, Object> replceMap = store.toReplaceMap();
        replceMap.putAll(item.toReplaceMap());

        // 处理导购问题
        if (item.getEmployeeId().isPresent()) {
            Optional<EmpEntity> emp = employeeAction.findById(item.getEmployeeId().get());
            emp.ifPresent(c -> replceMap.putAll(c.toReplaceMap()));
        }
        String replce = null;
        try {
            replce = tempate.get().replace(replceMap);
        } catch (TemplateReplaceException e) {
            logger.error(e.getMessage(), e);
            return new RetailFactAgg(item, store, tempate.get());
        }
        if (!StringUtils.startsWith(replce, "【"))
            replce = String.format("%s%s", smsPrefix, replce);

        try {
            smsBalanceAction.billing(store, smsCount(replce));
        } catch (SmsBillingException e) {
            logger.error("余额不足,计费失败", e);
            return new RetailFactAgg(item, store, tempate.get(), replce, String.format("余额不足,计费失败,storeId=%s", store.getId()));
        }
        RetailFactAgg agg = new RetailFactAgg(item, tempate.get(), store, replce);
        try {
            Message<RetailFactAgg> msg_request = MessageBuilder.withPayload(agg).build();
            if (messagingTemplate != null) {
                messagingTemplate.send(Constant.SUBSCRIBE_CHANNEL, msg_request);
            }
        } catch (Exception e) {
            logger.error("发布Message<RetailFactAgg> has error....", e);
        }
        return agg;
    }

    // 计算短息数量
    private static int smsCount(String content) {
        int smsCount = 0;
        int length = content.length();
        if (length < 71) {
            smsCount = 1;
        } else {
            if (length % 67 > 0) {
                smsCount = length / 67 + 1;
            } else {
                smsCount = length / 67;
            }
        }
        return smsCount;
    }

    private StoEntityAction storeAction;
    private TemplateEntityAction templateAction;
    private SmsBalanceEntityAction smsBalanceAction;
    private EmpEntityAction employeeAction;
    private SendSmsEntityAction sendSmsEntityAction;
    private MessagingTemplate messagingTemplate;

}
