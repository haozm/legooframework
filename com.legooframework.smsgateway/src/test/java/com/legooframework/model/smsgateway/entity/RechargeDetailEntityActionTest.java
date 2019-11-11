package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.covariant.entity.StoEntityAction;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class RechargeDetailEntityActionTest {

    @Test
    public void recharge() {
//        LoginContextHolder.setAnonymousCtx();
//        Optional<CrmOrganizationEntity> com = companyAction.findCompanyById(1);
//        StoEntity stre = storeAction.loadById(5);
//        RechargeRuleSet rules = ruleEntityAction.loadAllRuleSet();
//        Optional<RechargeRuleEntity> rule = rules.getSuitableRule(com.get().getId(), 300000);
//        rechargeDetailEntityAction.recharge(null, null, stre.get(),
//                null, null, rule.get(), 300000);
    }

    @Test
    public void loadStoreBalance() {
//        LoginContextHolder.setAnonymousCtx();
//        Optional<CrmOrganizationEntity> com = companyAction.findCompanyById(1);
//        StoEntity stre = storeAction.loadById(5);
//        Optional<List<RechargeDetailEntity>> res = rechargeDetailEntityAction.loadStoreBalance(stre);
//        System.out.println(res.get());
    }

    //    @Autowired
//    CrmOrganizationEntityAction companyAction;
    @Autowired
    StoEntityAction storeAction;
    @Autowired
    RechargeDetailEntityAction rechargeDetailEntityAction;
    @Autowired
    private RechargeRuleEntityAction ruleEntityAction;
}