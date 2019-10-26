package com.legooframework.model.regiscenter.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.regiscenter.entity.LoginContextTest;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/regiscenter/spring-model-cfg.xml"}
)
public class RegisCenterServiceTest {


    @Autowired
    RegisCenterService regisCenterService;

    @Test
    public void activedDevice() {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void changeDevice() {
        LoginContextHolder.setCtx(new LoginContextTest());
        regisCenterService.changeDevice("123", "456", 11);
    }
}