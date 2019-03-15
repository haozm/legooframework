package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeDetailEntityAction;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsgateway.entity.SMSSendRuleEntity;
import com.legooframework.model.smsgateway.entity.SMSSendRuleEntityAction;
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
public class SmsDeductionServiceTest {

    @Test
    public void deductionService() {
        LoginContextHolder.setAnonymousCtx();
        String ssm = "按照NPD的统计，任天堂Switch成了去年销量最高的游戏主机，这既证明老任的商业策略成功，" +
                "也暗示本世代的PS4和XBOX ONE已到暮年，是该更新了。最新一份来自国外的爆料显示，下一代Xbox共有两款，" +
                "一款代号Anaconda（蟒蛇），Navi核心GPU[%s]";
        CrmOrganizationEntity com = organizationEntityAction.findCompanyById(1).get();
        CrmStoreEntity store = storeAction.findById(com, 8).get();
        KvDictEntity businessType = dictEntityAction.findByValue("SMS_BUS_TYPE", "90TOUCHED").get();
        final SMSSendRuleEntity sendRule = ruleEntityAction.findByType(businessType);
        final String batch = "0000111122223333";
        List<SMSEntity> sms_list = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            sms_list.add(SMSEntity.createSMS(UUID.randomUUID().toString(), String.format(ssm, i), "13710276623",
                    null, null));
        }
        deductionService.charging(sms_list, businessType, store, false, "D的统计，任天堂Switch成了去年销量最高的游戏主机，这既证明老任");
    }


    @Resource(name = "smsKvDictEntityAction")
    KvDictEntityAction dictEntityAction;
    @Autowired
    CrmOrganizationEntityAction companyAction;
    @Autowired
    CrmStoreEntityAction storeAction;
    @Autowired
    CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    RechargeDetailEntityAction rechargeDetailEntityAction;
    @Autowired
    private SmsDeductionService deductionService;
    @Autowired
    SMSSendRuleEntityAction ruleEntityAction;
}