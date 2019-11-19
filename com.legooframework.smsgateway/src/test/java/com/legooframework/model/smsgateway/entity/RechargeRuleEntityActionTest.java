package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import org.joda.time.LocalDate;
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
public class RechargeRuleEntityActionTest {

    @Test
    public void loadAllRule() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<RechargeRuleEntity>> res = ruleEntityAction.loadAllRules();
        res.ifPresent(System.out::println);
    }

    @Test
    public void addTemporaryRule() {
        LoginContextHolder.setAnonymousCtx();
        OrgEntity com = orgEntityAction.loadComById(1);
//        Long min, Long max, double unitPrice, OrgEntity company, String remarks, LocalDate expiredDate
        ruleEntityAction.addTemporaryRule(30000L, 120000L, 9D, com, "一次性充值", null);
    }


    @Test
    public void disabled() {
        LoginContextHolder.setAnonymousCtx();
        ruleEntityAction.disabled("fSXOGZTCQmiWuTnp");
    }


    @Test
    public void modify() {
        LoginContextHolder.setAnonymousCtx();
        //tring ruleId, Long min, Long max, Double unitPrice
        // ruleEntityAction.modify("mSiFOnUvXmIOJ0WV", 10000L, 600000L, null);
    }

    @Autowired
    OrgEntityAction orgEntityAction;
    @Autowired
    private RechargeRuleEntityAction ruleEntityAction;

}