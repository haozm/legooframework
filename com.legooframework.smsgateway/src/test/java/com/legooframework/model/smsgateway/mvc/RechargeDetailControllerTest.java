package com.legooframework.model.smsgateway.mvc;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.smsgateway.entity.RechargeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
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
public class RechargeDetailControllerTest {

    @Test
    public void recharge() {
        LoginContextHolder.setAnonymousCtx();
        RechargeReqDto rechargeDto = new RechargeReqDto(1, 22, null, RechargeType.FreeCharge, 0D, 0,
                100, null);
        LoginContext user = LoginContextHolder.get();
        Message<RechargeReqDto> message = MessageBuilder.withPayload(rechargeDto)
                .setHeader("user", user)
                .build();
        messagingTemplate.send("channel_sms_balance", message);
//        Message<RechargeReqDto> message1 = MessageBuilder.withPayload(rechargeDto)
//                .setHeader("user", user)
//                .build();
//        messagingTemplate.sendAndReceive("channel_sms_balance", message1);
    }

    public static void main1(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"
        });
        MessagingTemplate msgt = app.getBean("smsMessagingTemplate", MessagingTemplate.class);
        LoginContextHolder.setAnonymousCtx();
        RechargeReqDto rechargeDto = new RechargeReqDto(1, 22, null, RechargeType.FreeCharge, 0D, 0,
                100, null);
        LoginContext user = LoginContextHolder.get();
        Message<RechargeReqDto> message = MessageBuilder.withPayload(rechargeDto)
                .setHeader("user", user)
                .build();
        for (int i = 0; i < 3; i++) {
            msgt.send("channel_sms_balance", message);
        }

    }

    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"
        });
        MessagingTemplate msgt = app.getBean("smsMessagingTemplate", MessagingTemplate.class);
        LoginContextHolder.setAnonymousCtx();
        LoginContext user = LoginContextHolder.get();
        String ssm = "按照NPD的统计，任天堂Switch成了去年销量最高的游戏主机，这既证明老任的商业策略成功，" +
                "也暗示本世代的PS4和XBOX ONE已到暮年，是该更新了。最新一份来自国外的爆料显示，下一代Xbox共有两款，" +
                "一款代号Anaconda（蟒蛇），Navi核心GPU[%s]";
//        CrmOrganizationEntity com = app.getBean(CrmOrganizationEntityAction.class).findCompanyById(1).get();
//        CrmStoreEntity store = app.getBean(CrmStoreEntityAction.class).findById(com, 8).get();
//        KvDictEntity businessType = app.getBean("smsKvDictEntityAction", KvDictEntityAction.class).findByValue("SMS_BUS_TYPE", "90TOUCHED").get();
//        final SMSSendRuleEntity sendRule = app.getBean(SMSSendRuleEntityAction.class).loadByType(BusinessType.TOUCHED90);
//        final String batch = "0000111122223333";
//        List<SMSEntity> sms_list = Lists.newArrayList();
//        for (int i = 0; i < 300; i++) {
//            sms_list.add(SMSEntity.createSMSMsg(UUID.randomUUID().toString(), null, "1371027", null, String.format(ssm, i)
//                    , 0));
//        }
//        DeductionReqDto rechargeDto = new DeductionReqDto(store, BusinessType.TOUCHED90, sms_list,
//                "计，任天堂Switch成了去年销量最高的游戏", SendMode.ManualBatch);
//        Message<DeductionReqDto> message = MessageBuilder.withPayload(rechargeDto)
//                .setHeader("user", user)
//                .build();
        // msgt.send(BundleService.CHANNEL_SMS_CHARGE, message);
    }


    public static void main3(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"
        });
    }

    @Resource(name = "smsMessagingTemplate")
    MessagingTemplate messagingTemplate;

}