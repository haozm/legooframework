package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class RechargeDetailEntityActionTest {

    @Test
    public void recharge() {
        LoginContextHolder.setAnonymousCtx();
        Optional<CrmOrganizationEntity> com = companyAction.findCompanyById(1);
        Optional<CrmStoreEntity> stre = storeAction.findById(com.get(), 5);
        RechargeRuleSet rules = ruleEntityAction.loadAllRuleSet();
//        Optional<RechargeRuleEntity> rule = rules.getSuitableRule(com.get().getId(), 300000);
//        rechargeDetailEntityAction.recharge(null, null, stre.get(),
//                null, null, rule.get(), 300000);
    }

    @Test
    public void loadStoreBalance() {
        LoginContextHolder.setAnonymousCtx();
        Optional<CrmOrganizationEntity> com = companyAction.findCompanyById(1);
        Optional<CrmStoreEntity> stre = storeAction.findById(com.get(), 5);
        Optional<List<RechargeDetailEntity>> res = rechargeDetailEntityAction.loadStoreBalance(stre.get());
        System.out.println(res.get());
    }

    @Autowired
    CrmOrganizationEntityAction companyAction;
    @Autowired
    CrmStoreEntityAction storeAction;
    @Autowired
    RechargeDetailEntityAction rechargeDetailEntityAction;
    @Autowired
    private RechargeRuleEntityAction ruleEntityAction;
}