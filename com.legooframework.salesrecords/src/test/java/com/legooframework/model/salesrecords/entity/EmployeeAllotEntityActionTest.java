package com.legooframework.model.salesrecords.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml"}
)
public class EmployeeAllotEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findById() {
        employeeAllotEntityAction.findById(4219793).ifPresent(x -> System.out.println(x));
    }

    @Autowired
    private EmployeeAllotEntityAction employeeAllotEntityAction;
}