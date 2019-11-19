package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsprovider/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/entities/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class RechargeDetailEntityActionTest {

    @Test
    public void rechargeByCompany() {
        LoginContextHolder.setAnonymousCtx();
        OrgEntity company = orgEntityAction.loadComById(1);
        RechargeRuleSet rules = ruleEntityAction.loadAllRuleSet();
        Optional<RechargeRuleEntity> rule = rules.getSuitableRule(company, 300000);
        rechargeDetailEntityAction.recharge(company, null, null, rule.get(), 300000);
    }

    @Test
    public void rechargeByStore() {
        LoginContextHolder.setAnonymousCtx();
        OrgEntity company = orgEntityAction.loadComById(1);
        StoEntity store = storeAction.loadById(1120);
        RechargeRuleSet rules = ruleEntityAction.loadAllRuleSet();
        Optional<RechargeRuleEntity> rule = rules.getSuitableRule(company, 300000);
        rechargeDetailEntityAction.recharge(company, store, null, rule.get(), 300000);
    }

    @Test
    public void rechargeByStoreGroup() {
        LoginContextHolder.setAnonymousCtx();
        OrgEntity company = orgEntityAction.loadComById(1);
        Optional<List<StoEntity>> stores = storeAction.findByIds(27, 28, 29, 30, 31, 32, 33);
        RechargeRuleSet rules = ruleEntityAction.loadAllRuleSet();
        Optional<RechargeRuleEntity> rule = rules.getSuitableRule(company, 300000);
        RechargeResDto rechargeResDto = rechargeDetailEntityAction.recharge(company, null, stores.get(), rule.get(), 500000);
        System.out.println(rechargeResDto);
    }

    @Test
    public void preRechargeByStoreGroup() {
        LoginContextHolder.setAnonymousCtx();
        OrgEntity company = orgEntityAction.loadComById(1);
        Optional<List<StoEntity>> stores = storeAction.findByIds(27, 28, 29, 30, 31, 32, 33);
        RechargeRuleSet rules = ruleEntityAction.loadAllRuleSet();
        Optional<RechargeRuleEntity> rule = rules.getSuitableRule(company, 300000);
        rechargeDetailEntityAction.precharge(company, null, stores.get(), rule.get(), 300000);
    }

    @Test
    public void loadStoreBalance() {
//        LoginContextHolder.setAnonymousCtx();
//        Optional<CrmOrganizationEntity> com = companyAction.findCompanyById(1);
//        StoEntity stre = storeAction.loadById(5);
//        Optional<List<RechargeDetailEntity>> res = rechargeDetailEntityAction.loadStoreBalance(stre);
//        System.out.println(res.get());
    }

    @Autowired
    OrgEntityAction orgEntityAction;
    @Autowired
    StoEntityAction storeAction;
    @Autowired
    RechargeDetailEntityAction rechargeDetailEntityAction;
    @Autowired
    private RechargeRuleEntityAction ruleEntityAction;
}