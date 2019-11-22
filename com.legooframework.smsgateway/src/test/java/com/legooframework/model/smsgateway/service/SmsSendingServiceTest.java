package com.legooframework.model.smsgateway.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.AutoRunChannel;
import com.legooframework.model.smsgateway.entity.SendMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.stream.Collectors;

public class SmsSendingServiceTest {
    //    HttpRequestExecutingMessageHandler
//    AbstractHttpRequestExecutingMessageHandler
//    LoggingHandler

    private static final Logger logger = LoggerFactory.getLogger(SmsSendingServiceTest.class);

    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(new String[]{
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsprovider/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"
        });
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        String sql = "SELECT cm.id from acp.crm_member cm \n" +
                "inner JOIN acp.crm_store_member csm on csm.member_id =cm.id\n" +
                "where csm.store_id=1120 and cm.status=1 limit 30";
        List<Integer> cids = app.getBean("smsJdbcQuerySupport", JdbcQuerySupport.class).getJdbcTemplate()
                .queryForList(sql, Integer.class);
        logger.debug(String.format("jdbcQuerySupport() mmdis is %d", cids.size()));
        StoEntity store = app.getBean(StoEntityAction.class).loadById(1120);
        UserAuthorEntity user = app.getBean(UserAuthorEntityAction.class).loadUserById(15, 1);
        List<SendMessageBuilder> msgList = cids.stream().map(x -> SendMessageBuilder.createWithoutJobNoTemplate(BusinessType.BIRTHDAYTOUCH,
                x, AutoRunChannel.SMS_ONLY)).collect(Collectors.toList());
        app.getBean(SmsGatewayService.class).batchSaveMessage(store, msgList,
                "【新的梦特娇】亲爱的{会员姓名}，内衣是贴身衣物，需要定期消毒喔，我们有专业的内衣消毒柜，您可以把内衣一起拿过来，为您一起消毒喔！", user);
    }
}