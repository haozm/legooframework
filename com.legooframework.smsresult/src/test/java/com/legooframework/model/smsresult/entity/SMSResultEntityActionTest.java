package com.legooframework.model.smsresult.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-smsresult-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/entities/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsresult/spring-model-cfg.xml"}
)
public class SMSResultEntityActionTest {

    @Test
    public void load4SyncState() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<Map<String, Object>>> asd = resultEntityAction.load4SyncState(2, 24);
        asd.ifPresent(x -> System.out.println(x));
    }


    @Autowired
    private SMSResultEntityAction resultEntityAction;
}