package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-smsclient-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SMSTransportLogEntityActionTest {

    @Test
    public void batchInsert() {
        LoginContextHolder.setAnonymousCtx();
        String ssm = "按照NPD的统计，任天堂Switch成了去年销量最高的游戏主机，这既证明老任的商业策略成功，" +
                "也暗示本世代爆料显示，下一代Xbox共有两款，" +
                "一款代号Anaconda（蟒蛇），Navi核心GPU[%s]";
//        CrmOrganizationEntity com = organizationEntityAction.findCompanyById(1).get();
//        CrmStoreEntity store = storeEntityAction.findById(com, 1314).get();
//        // KvDictEntity businessType = dictEntityAction.findByValue("SMS_BUS_TYPE", "90TOUCHED").get();
//        final SMSSendRuleEntity sendRule = smsSendRuleEntityAction.loadByType(BusinessType.TOUCHED90);
//        final String batch = "0000111122223333";
//        List<SMSEntity> sms_list = Lists.newArrayList();
//        for (int i = 0; i < 10; i++) {
//            sms_list.add(SMSEntity.createSMSMsg(UUID.randomUUID().toString(), -1, "13710276623", null, String.format(ssm, i),
//                    RandomUtils.nextInt()));
//        }

//        List<SendMsg4InitEntity> alo = Lists.newArrayList();
//        sms_list.forEach(x -> {
//            //CrmStoreEntity store, SMSEntity sms, String smsBatchNo, SMSSendRuleEntity sendRule
//           // alo.add(SendMsg4InitEntity.createManualBatch(store, x, batch, sendRule));
//        });
//        action.batchInsert(alo);
    }

    @Test
    public void loadSms4WriteOff() {
        LoginContextHolder.setAnonymousCtx();
        // action.loadSms4WriteOff();
    }

    @Autowired
    SMSSendRuleEntityAction smsSendRuleEntityAction;
    //    @Autowired
//    CrmOrganizationEntityAction organizationEntityAction;
//    @Autowired
//    CrmStoreEntityAction storeEntityAction;
    @Autowired
    private SendMsgStateEntityAction action;
}