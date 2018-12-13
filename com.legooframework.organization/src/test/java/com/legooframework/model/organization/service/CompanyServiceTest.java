package com.legooframework.model.organization.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.organization.LoginContextTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/organization/spring-model-cfg.xml"}
)
public class CompanyServiceTest {

    @Test
    public void addStoreToCompany() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
        Thread.sleep(5000L);
    }

    @Autowired
    CompanyService companyManagerService;
}