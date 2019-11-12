package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)

public class SMSBatchSendLogEntityActionTest {

    @Test
    public void updateSendResulset() {
    }

    @Test
    public void loadSms4Sending() {
        LoginContextHolder.setAnonymousCtx();
//        Optional<List<SendMsg4SendEntity>> sendLogs = action.loadSms4Sending("1_22_tJvvhG9kW4E0");
//        sendLogs.ifPresent(logs -> {
//            Message<List<SendMsg4SendEntity>> msg_request = MessageBuilder.withPayload(logs)
//                    .setHeader("user", LoginContextHolder.get())
//                    .build();
//            messagingTemplate.send(BundleService.CHANNEL_SMS_VERIFY, msg_request);
//        });
    }

    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"
        });
        MessagingTemplate msgt = app.getBean("smsMessagingTemplate", MessagingTemplate.class);
        LoginContextHolder.setAnonymousCtx();
//        Optional<List<SendMsg4SendEntity>> sendLogs = app.getBean(SendMsg4SendEntityAction.class)
//                .loadSms4Sending("1_22_1vHC6wVN9Vbw");
//        sendLogs.ifPresent(logs -> {
//            Message<List<SendMsg4SendEntity>> msg_request = MessageBuilder.withPayload(logs)
//                    .setHeader("user", LoginContextHolder.get())
//                    .build();
//            // msgt.send(BundleService.CHANNEL_SMS_VERIFY, msg_request);
//        });
    }


    @Resource(name = "smsMessagingTemplate")
    MessagingTemplate messagingTemplate;
}