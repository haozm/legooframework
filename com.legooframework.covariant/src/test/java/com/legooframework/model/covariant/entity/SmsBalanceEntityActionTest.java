package com.legooframework.model.covariant.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class SmsBalanceEntityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findByOrg() {
        Optional<StoEntity> sto = stoEntityAction.findById(1120);
        smsBalanceEntityAction.find4Com(sto.get()).ifPresent(c -> System.out.println(c));
    }

    @Test
    public void findByStore() {
        Optional<StoEntity> sto = stoEntityAction.findById(1120);
        smsBalanceEntityAction.find4Store(sto.get()).ifPresent(c -> System.out.println(c));
    }

    @Test
    public void billing() {
        Optional<StoEntity> sto = stoEntityAction.findById(4);
        smsBalanceEntityAction.billing(sto.get(), 10000);
    }

    @Autowired
    private SmsBalanceEntityAction smsBalanceEntityAction;
    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private OrgEntityAction orgEntityAction;
}