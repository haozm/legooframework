package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.CommonsUtils;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import com.legooframework.model.membercare.entity.BusinessType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SMSBatchSaveLogEntityActionTest {

    @Test
    public void batchInsert() {
        LoginContextHolder.setAnonymousCtx();
        String ssm = "【动感曲线清溪店】尊贵的卿艳辉顾客您好：感谢您长期以来对我们的支持与信任，本月是您的生日，欢迎到店免费领取回复TD退订";
        List<MsgEntity> smses = Lists.newArrayList();
        for (int i = 0; i < 3000; i++) {
            smses.add(MsgEntity.createSMSMsgWithNoJob(UUID.randomUUID().toString(), -1, "18588828127",
                    null, String.format(ssm, i), null));
        }
        KvDictEntity businessType = kvDictEntityAction.findByValue("SMS_BUS_TYPE", "90TOUCHED").get();
        final SMSSendRuleEntity sendRule = smsSendRuleEntityAction.loadByType(BusinessType.TOUCHED90);
        final String sms_batch_no = String.format("%s_%s_%s", 1, 22, CommonsUtils.randomId(12));
//        List<SendMsg4InitEntity> batchSaveLogs = smses.stream()
//                .map(x -> new SendMsg4InitEntity(1, 22, x, sms_batch_no, sendRule, SendMode.ManualBatch)).collect(Collectors.toList());
//        action.batchInsert(batchSaveLogs);
    }

    @Autowired
    SendMsgStateEntityAction action;
    @Autowired
    SMSSendRuleEntityAction smsSendRuleEntityAction;
    @Resource(name = "smsKvDictEntityAction")
    KvDictEntityAction kvDictEntityAction;
}