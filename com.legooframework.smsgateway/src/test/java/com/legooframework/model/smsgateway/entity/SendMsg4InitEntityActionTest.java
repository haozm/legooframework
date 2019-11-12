package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SendMsg4InitEntityActionTest {

    @Test
    public void batchInsert() {
        LoginContextHolder.setAnonymousCtx();
        List<SMSEntity> smses = Lists.newArrayList();
        String batchNo = UUID.randomUUID().toString();
        StoEntity store = stoEntityAction.loadById(1120);
        for (int i = 0; i < 10; i++) {
            SMSEntity sms = SMSEntity.createSMSMsgWithNoJob(UUID.randomUUID().toString(), 12, "18588828127", "HAOXIAOJIE",
                    String.format("【武商广场沙驰男装】您好HXJ，在穿着过程中有搭配方面的问题吗，可以随时到店帮您搭配哦，请记得洗涤保养技巧--%d，祝您愉快每一天。退订回T", i));
            smses.add(sms);
        }
        sendMsg4InitEntityAction.batchMarketChannelInsert(smses, store, batchNo, false, BusinessType.COMMONSMS);
    }

    @Test
    public void loadNeedSyncStateSmsIds() {
        Optional<List<String>> lisr = sendMsg4InitEntityAction.loadNeedSyncStateSmsIds();
        lisr.ifPresent(asd-> System.out.println(asd));
    }


    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private SendMsg4InitEntityAction sendMsg4InitEntityAction;
}