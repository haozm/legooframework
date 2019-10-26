package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
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
public class RechargeRuleEntityActionTest {

    @Test
    public void loadAllRule() {
        LoginContextHolder.setAnonymousCtx();
        //Optional<List<RechargeRuleEntity>> res = ruleEntityAction.loadAllRule();
       // res.ifPresent(System.out::println);
    }

    @Test
    public void addRule() {
        LoginContextHolder.setAnonymousCtx();
        //ruleEntityAction.addRule(30000L, 120000L, 9D, null, false, "一次性充值", null);
    }


    @Test
    public void disabled() {
        LoginContextHolder.setAnonymousCtx();
        ruleEntityAction.disabled("cc5tlQG4Z8oNc5sX");
    }


    @Test
    public void modify() {
        LoginContextHolder.setAnonymousCtx();
        //tring ruleId, Long min, Long max, Double unitPrice
       // ruleEntityAction.modify("mSiFOnUvXmIOJ0WV", 10000L, 600000L, null);
    }

    @Autowired
    private RechargeRuleEntityAction ruleEntityAction;

}