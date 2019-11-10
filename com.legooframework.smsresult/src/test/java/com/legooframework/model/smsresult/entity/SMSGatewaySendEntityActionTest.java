package com.legooframework.model.smsresult.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-smsresult-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/entities/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsresult/spring-model-cfg.xml"}
)
public class SMSGatewaySendEntityActionTest {

    @Test
    public void insert() {
        LoginContextHolder.setAnonymousCtx();
        SMSEntity sendSMS = SMSEntity.create4Sending(UUID.randomUUID().toString(), "【动感曲线清溪店】尊贵的卿艳辉顾客您好：感谢您长期以来对我们的支持与信任，本月是您的生日，欢迎到店免费领取回复TD退订",
                "18588828127", 67, 1);
        // action.insert(1, 22, sendSMS, 1, 2, RandomUtils.nextLong(1, 10000000000L));
    }

    @Test
    public void insertA() {
        LoginContextHolder.setAnonymousCtx();
        SMSEntity sendSMS = SMSEntity.create4Sending(UUID.randomUUID().toString(), "【动感曲线清溪店】尊贵的卿艳辉顾客您好：感谢您长期以来对我们的支持与信任，本月是您的生日，欢迎到店免费领取回复TD退订",
                "18588828127", 67, 1);
        // action.insertA(100098, 9901, sendSMS, 1, 2, RandomUtils.nextLong(1, 10000000000L));
    }

    @Test
    public void load4Sending() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Autowired
    SMSResultEntityAction action;

}