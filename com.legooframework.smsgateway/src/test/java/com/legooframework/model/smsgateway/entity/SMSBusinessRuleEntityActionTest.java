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
public class SMSBusinessRuleEntityActionTest {

    @Test
    public void addRule() {
        LoginContextHolder.setAnonymousCtx();
        //ruleEntityAction.addRule("80", SMSChannel.MarketChannel, true);
    }

    @Test
    public void findByType() {
        LoginContextHolder.setAnonymousCtx();
//        Optional<SMSSendRuleEntity> res = ruleEntityAction.loadByType("90");
//        res.ifPresent(System.out::println);
    }

    @Test
    public void modify() {
    }

    @Autowired
    SMSSendRuleEntityAction ruleEntityAction;
}