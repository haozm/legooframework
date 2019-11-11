package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SendMsg4InitEntityActionTest {

    @Test
    public void batchInsert() {
        List<SendMsg4InitEntity> smses = Lists.newArrayList();
        String batchNo = LocalDateTime.now().toString("yyyyMMddHHmmss");
        StoEntity store = stoEntityAction.loadById(1120);
        for (int i = 0; i < 100; i++) {
            SMSEntity sms = SMSEntity.createSMSMsgWithNoJob(UUID.randomUUID().toString(), 12, "18588828127", "HAOXIAOJIE",
                    String.format("【武商广场沙驰男装】您好HXJ，在穿着过程中有搭配方面的问题吗，可以随时到店帮您搭配哦，请记得洗涤保养技巧--%d，祝您愉快每一天。退订回T", i));
            SendMsg4InitEntity sabe = SendMsg4InitEntity.createInstance(store, sms, batchNo, SMSChannel.MarketChannel, false, BusinessType.TESTSMS);
            smses.add(sabe);
        }
        sendMsg4InitEntityAction.batchInsert(smses);
    }

    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private SendMsg4InitEntityAction sendMsg4InitEntityAction;
}