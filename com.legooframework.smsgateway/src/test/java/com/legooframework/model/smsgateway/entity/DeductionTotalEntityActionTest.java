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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class DeductionTotalEntityActionTest {

    @Test
    public void insert() {
        LoginContextHolder.setAnonymousCtx();
        //Optional<KvDictEntity> buty = kvDictEntityAction.findByValue("SMS_BUS_TYPE", "90TOUCHED");
//        SMSSendRuleEntity rule = businessRuleEntityAction.loadByType(BusinessType.TOUCHED90);
//        CrmOrganizationEntity com = organizationEntityAction.findCompanyById(1).get();
//        CrmStoreEntity store = storeEntityAction.findById(com, 5).get();
//        billingSummaryEntityAction.insertManual(store, rule,
//                String.format("%s_%s_201981212771212", store.getCompanyId(), store.getId()),
//                9966, "asdasdasdasdasd");
    }


    //    @Autowired
//    CrmOrganizationEntityAction organizationEntityAction;
//    @Autowired
//    CrmStoreEntityAction storeEntityAction;
    @Autowired
    SMSSendRuleEntityAction businessRuleEntityAction;
}