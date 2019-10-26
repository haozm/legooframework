package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.crmadapter.service.CrmReadService;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsgateway.entity.SendMode;
import com.legooframework.model.smsgateway.mvc.DeductionReqDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.http.outbound.AbstractHttpRequestExecutingMessageHandler;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SmsSendingServiceTest {
    //    HttpRequestExecutingMessageHandler
//    AbstractHttpRequestExecutingMessageHandler
//    LoggingHandler
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-smsclient-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsprovider/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"
        });
        List<SMSEntity> smses = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            SMSEntity msm = SMSEntity.createSMSMsg(UUID.randomUUID().toString(), 12, "18588828127", "hxj",
                    "【测试公司】回家吃饭，你妈妈喊你了.回复TD退订", 0);
            smses.add(msm);
        }
        LoginContext user = app.getBean(CrmReadService.class).loadByLoginName(100098, "00003HBCYMTJ");
        Optional<CrmOrganizationEntity> company = app.getBean(CrmOrganizationEntityAction.class).findCompanyById(100098);
        Optional<CrmStoreEntity> store = app.getBean(CrmStoreEntityAction.class).findById(company.get(), 1315);
        DeductionReqDto reqDto = new DeductionReqDto(store.get(), BusinessType.TOUCHED90, smses, "【测试公司】回家吃饭，你妈妈喊你了.回复TD退订",
                SendMode.ManualBatch);
//        Message<DeductionReqDto> message = MessageBuilder.withPayload(reqDto).setHeader("user", user)
//                .setHeader("action", "charge").build();
//        Message<?> mess = app.getBean("smsMessagingTemplate", MessagingTemplate.class).sendAndReceive("channel_sms_billing", message);
        //  System.out.println(mess.getPayload());
        app.getBean(SmsIntegrationService.class).billingAndSettlement(user, "charge", reqDto);
    }
}