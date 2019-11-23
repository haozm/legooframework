package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.membercare.entity.BusinessType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class DeductionDetailEntityActionTest {

    @Test
    public void batchInsert() {
        LoginContextHolder.setAnonymousCtx();
        // Optional<KvDictEntity> buty = kvDictEntityAction.findByValue("SMS_BUS_TYPE", "90TOUCHED");
        SMSSendRuleEntity rule = businessRuleEntityAction.loadByType(BusinessType.TOUCHED90);
//        CrmOrganizationEntity com = organizationEntityAction.findCompanyById(1).get();
//        CrmStoreEntity store = storeEntityAction.findById(com, 8).get();
//        RechargeBalanceList balanceList = balanceEntityAction.loadOrderEnabledByStore(store);
//        System.out.println(balanceList);
//        List<ChargeDetailEntity> asd = balanceList.deduction(store, "1_5_201981212771212", 40000);
//        System.out.println(asd);
//        ChargeDetailEntity bill = new ChargeDetailEntity("1_5_201981212771212",  store, RechargeBalanceEntity balance);
//        asd.add();
        //  billingDetailEntityAction.batchInsert(asd);
    }

    @Autowired
    DeductionDetailEntityAction billingDetailEntityAction;

    //    @Autowired
//    CrmOrganizationEntityAction organizationEntityAction;
//    @Autowired
//    CrmStoreEntityAction storeEntityAction;
    @Autowired
    SMSSendRuleEntityAction businessRuleEntityAction;
    @Autowired
    RechargeBalanceEntityAction balanceEntityAction;
}