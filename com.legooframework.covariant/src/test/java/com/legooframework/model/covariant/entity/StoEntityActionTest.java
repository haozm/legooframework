package com.legooframework.model.covariant.entity;

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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class StoEntityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findById() {
        stoEntityAction.findById(2).ifPresent(c -> System.out.println(c));
    }


    @Test
    public void findByOld() {
        stoEntityAction.findByOldInfo("A0001", "DGQX").ifPresent(c -> System.out.println(c));
        stoEntityAction.findByOldInfo("A0001", "DGQX").ifPresent(c -> System.out.println(c));
    }

    @Autowired
    private StoEntityAction stoEntityAction;
}