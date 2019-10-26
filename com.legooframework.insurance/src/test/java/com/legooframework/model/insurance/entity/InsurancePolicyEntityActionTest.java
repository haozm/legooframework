package com.legooframework.model.insurance.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-insurance-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/insurance/spring-model-cfg.xml"}
)
public class InsurancePolicyEntityActionTest {

    @Test
    public void findByInsuranceNo() {
        LoginContextHolder.setAnonymousCtx();
        policyEntityAction.findByInsuranceNo("PX0012030100230123");
    }

    @Autowired
    InsurancePolicyEntityAction policyEntityAction;
}