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
public class MsgTemplateEnityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findBirthCareTemplet4Store() {
        StoEntity store = stoEntityAction.loadById(1120);
        msgTemplateEnityAction.findOneBirthCareTemplet4Store(store)
                .ifPresent(x -> System.out.println(x));
    }

    @Test
    public void findById() {
        msgTemplateEnityAction.findById(60)
                .ifPresent(x -> System.out.println(x));
    }

    @Autowired
    private MsgTemplateEnityAction msgTemplateEnityAction;
    @Autowired
    private StoEntityAction stoEntityAction;
}