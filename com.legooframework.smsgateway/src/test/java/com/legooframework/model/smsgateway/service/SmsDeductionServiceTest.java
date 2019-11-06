package com.legooframework.model.smsgateway.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeDetailEntityAction;
import com.legooframework.model.smsgateway.entity.SMSSendRuleEntityAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SmsDeductionServiceTest {

    @Test
    public void deductionService() {
        LoginContextHolder.setAnonymousCtx();
        String ssm = "按照NPD的统计，任天堂Switch成了去年销量最高的游戏主机，这既证明老任的商业策略成功，" +
                "也暗示本世代的PS4和XBOX ONE已到暮年，是该更新了。最新一份来自国外的爆料显示，下一代Xbox共有两款，" +
                "一款代号Anaconda（蟒蛇），Navi核心GPU[%s]";
//        CrmOrganizationEntity com = organizationEntityAction.findCompanyById(1).get();
//        CrmStoreEntity store = storeAction.findById(com, 8).get();
//        KvDictEntity businessType = dictEntityAction.findByValue("SMS_BUS_TYPE", "90TOUCHED").get();
//        final SMSSendRuleEntity sendRule = ruleEntityAction.loadByType(BusinessType.TOUCHED90);
//        final String batch = "0000111122223333";
//        List<SMSEntity> sms_list = Lists.newArrayList();
//        for (int i = 0; i < 10; i++) {
//            sms_list.add(SMSEntity.createSMSMsg(UUID.randomUUID().toString(), -1, null, "13710276623", String.format(ssm, i),0));
//        }
        // deductionService.charging(sms_list, BusinessType.TOUCHED90, store, false, "D的统计，任天堂Switch成了去年销量最高的游戏主机，这既证明老任");
    }


    @Resource(name = "smsKvDictEntityAction")
    KvDictEntityAction dictEntityAction;
    @Autowired
    RechargeDetailEntityAction rechargeDetailEntityAction;
    @Autowired
    SMSSendRuleEntityAction ruleEntityAction;
}