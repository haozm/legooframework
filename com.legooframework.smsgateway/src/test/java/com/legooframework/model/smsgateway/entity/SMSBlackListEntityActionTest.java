package com.legooframework.model.smsgateway.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SMSBlackListEntityActionTest {

    @Test
    public void uneffective() {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx(1L));
        blackListEntityAction.uneffective("18588828127", 1);
    }

    @Test
    public void effective() {
        LoginContextHolder.setCtx(LoginContextHolder.getAnonymousCtx(1L));
        blackListEntityAction.effective("18588828127", 1);
    }

    @Test
    public void diabled() {
        LoginContextHolder.setAnonymousCtx();
        List<String> list = Lists.newArrayList("18588828127", "18588828126", "18588828125", "18228828127");
        blackListEntityAction.diabled(list, 1);
    }

    @Test
    public void findById() {
    }


    @Autowired
    SMSBlackListEntityAction blackListEntityAction;
}