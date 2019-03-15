package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.smsgateway.entity.ChargeSummaryEntity;
import com.legooframework.model.smsgateway.entity.ChargeSummaryEntityAction;
import com.legooframework.model.smsgateway.entity.SMSTransportLogEntity;
import com.legooframework.model.smsgateway.entity.SMSTransportLogEntityAction;
import com.legooframework.model.smsgateway.filter.SmsSendInterceptor;
import com.legooframework.model.smsgateway.mvc.DeductionReqDto;
import com.legooframework.model.smsgateway.mvc.RechargeReqDto;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SmsIntegrationService extends SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SmsIntegrationService.class);

    public Message<?> charging(@Header(name = "user") LoginContext user, @Payload Object object) {
        LoginContextHolder.setCtx(user);
        if (object instanceof RechargeReqDto) {
            RechargeReqDto rechargeDto = (RechargeReqDto) object;
            TransactionStatus ts = startTx(null);
            try {
                if (rechargeDto.isFreeCharge()) {
                    getBean(SMSRechargeService.class).freecharge(rechargeDto.getCompanyId(), rechargeDto.getStoreGroupId(),
                            rechargeDto.getStoreId(), rechargeDto.getTotalQuantity());
                } else {
                    if (rechargeDto.hasUnitPrice()) {
                        if (rechargeDto.isStoreRange()) {
                            getBean(SMSRechargeService.class).rechargeByStoreOnce(rechargeDto.getCompanyId(),
                                    rechargeDto.getStoreId(),
                                    rechargeDto.getRechargeAmount(), rechargeDto.getUnitPrice(), rechargeDto.getRemarke(),
                                    rechargeDto.getRechargeType());
                        } else if (rechargeDto.isStoreGroupRange()) {
                            getBean(SMSRechargeService.class).rechargeByStoreGroupOnce(rechargeDto.getCompanyId(),
                                    rechargeDto.getStoreGroupId(),
                                    rechargeDto.getRechargeAmount(), rechargeDto.getUnitPrice(), rechargeDto.getRemarke(),
                                    rechargeDto.getRechargeType());
                        } else {
                            getBean(SMSRechargeService.class).rechargeByCompanyOnce(rechargeDto.getCompanyId(),
                                    rechargeDto.getRechargeAmount(), rechargeDto.getUnitPrice(), rechargeDto.getRemarke(),
                                    rechargeDto.getRechargeType());
                        }
                    } else {
                        if (rechargeDto.isStoreRange()) {
                            getBean(SMSRechargeService.class).rechargeByStore(rechargeDto.getCompanyId(), rechargeDto.getStoreId(),
                                    rechargeDto.getRechargeAmount(), rechargeDto.getRechargeType());
                        } else if (rechargeDto.isStoreGroupRange()) {
                            getBean(SMSRechargeService.class).rechargeByStoreGroup(rechargeDto.getCompanyId(),
                                    rechargeDto.getStoreGroupId(),
                                    rechargeDto.getRechargeAmount(), rechargeDto.getRechargeType());
                        } else {
                            getBean(SMSRechargeService.class).rechargeByCompany(rechargeDto.getCompanyId(),
                                    rechargeDto.getRechargeAmount(), rechargeDto.getRechargeType());
                        }
                    }
                }
                commitTx(ts);
            } catch (Exception e) {
                logger.error(String.format("recharge(%s) has error...", rechargeDto), e);
                rollbackTx(ts);
                return MessageBuilder.withPayload(e).build();
            }
        } else if (object instanceof DeductionReqDto) {
            DeductionReqDto dto = (DeductionReqDto) object;
            TransactionStatus ts = startTx(null);
            String summary_id;
            try {
                summary_id = getBean(SmsDeductionService.class).charging(dto.getSmses(), dto.getBusinessType(),
                        dto.getStore(), false, dto.getSmsContext());
                commitTx(ts);
            } catch (Exception e) {
                logger.error(String.format("SmsDeductionService.charging(%s) has error...", dto), e);
                rollbackTx(ts);
                return MessageBuilder.withPayload(e).build();
            }
            Optional<ChargeSummaryEntity> chargeSummary = getBean(ChargeSummaryEntityAction.class).findById(summary_id);
            Preconditions.checkState(chargeSummary.isPresent(), "Id=%s 对应的充值统计不存在...", summary_id);
            getBean(SMSTransportLogEntityAction.class).updateLog4Storage(chargeSummary.get());
            try {
                Message<ChargeSummaryEntity> msg_request = MessageBuilder.withPayload(chargeSummary.get())
                        .setHeader("user", user)
                        .build();
                getMessagingTemplate().send("channel_event_bus", msg_request);
            } catch (Exception e) {
                logger.error(String.format("[Event-Driver]事件发布失败.....%s", chargeSummary), e);
            }
        } else {
            Exception e = new IllegalArgumentException(String.format("非法的入参 %s", object));
            return MessageBuilder.withPayload(e).build();
        }
        return MessageBuilder.withPayload("ok").build();
    }

    public void subscribeSMSEvent(@Header(name = "user") LoginContext user, @Payload Object payload) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("subscribeSMSEvent(%s) ", payload));
        if (payload instanceof ChargeSummaryEntity) {
            ChargeSummaryEntity chargeSummary = (ChargeSummaryEntity) payload;
            Optional<List<SMSTransportLogEntity>> transportLogs = getBean(SMSTransportLogEntityAction.class).loadSms4Sending(chargeSummary);
            try {
                transportLogs.ifPresent(logs -> {
                    Message<List<SMSTransportLogEntity>> msg_request = MessageBuilder.withPayload(logs)
                            .setHeader("user", user)
                            .build();
                    getMessagingTemplate().send("channel_checking_bus", msg_request);
                });
            } catch (Exception e) {
                logger.error(String.format("[Event-Driver]按照批次%s 加载发布待发送短信并发布错误", chargeSummary), e);
            }
        }
    }

    public void beforeSendInterceptor(@Header(name = "user") LoginContext user, @Payload List<SMSTransportLogEntity> transportLogs) {
        if (CollectionUtils.isNotEmpty(transportLogs)) {
            getBean("smsBeforeSendInterceptor", SmsSendInterceptor.class).filter(transportLogs);
            List<SMSTransportLogEntity> errors = transportLogs.stream().filter(SMSTransportLogEntity::isError)
                    .collect(Collectors.toList());
            List<SMSTransportLogEntity> sendings = transportLogs.stream().filter(x -> !x.isError())
                    .collect(Collectors.toList());
            if (logger.isDebugEnabled())
                logger.debug(String.format("filter(%s) error = %s,sendings =%s", transportLogs.size(),
                        CollectionUtils.isEmpty(errors) ? 0 : errors.size(), CollectionUtils.isEmpty(sendings) ? 0 : sendings.size()));
            if (CollectionUtils.isNotEmpty(errors)) {
                errors.stream().map(x -> MessageBuilder.withPayload(x).setHeader("user", user).build())
                        .forEach(x -> getMessagingTemplate().send("sms_error_channel", x));
            }
            if (CollectionUtils.isNotEmpty(sendings)) {
                sendings.stream().map(x -> MessageBuilder.withPayload(x).setHeader("user", user).build())
                        .forEach(x -> getMessagingTemplate().send("sms_sending_channel", x));
            }
        }
    }

    public void handleError(@Header(name = "user") LoginContext user, @Payload SMSTransportLogEntity transportLog) {
        getBean(SMSTransportLogEntityAction.class).updateErrorInstance(transportLog);
    }

}
