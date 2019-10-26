package com.legooframework.model.autotask.entity;

import com.legooframework.model.batchsupport.entity.LoginContextTest;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-autotask-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/autotask/spring-model-cfg.xml"}
)
public class TaskRuleEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void addRule() {
        OrgEntity com = companyAction.loadComById(1);
        StoEntity sto = storeAction.loadById(1120);
        taskRuleEntityAction.addRule(com, sto, BusinessType.HOLIDAYCARE, DelayType.NO_DELAY, "day=0,hour=0,minute=0",
                SendChannel.SMS, RoleType.Member, "{会员姓名}你妈妈含义回家吃饭 公司");
    }

    @Test
    public void findAll() {
        taskRuleEntityAction.findAll();
    }

    @Autowired
    OrgEntityAction companyAction;
    @Autowired
    StoEntityAction storeAction;
    @Autowired
    TaskRuleEntityAction taskRuleEntityAction;

}