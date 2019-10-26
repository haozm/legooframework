package com.legooframework.model.crmjob.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmjob/spring-model-cfg.xml"}
)
public class DeviceWithWeixinEntityActionTest {

    @Test
    public void loadAll() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<List<DeviceWithWeixinEntity>> asd = action.loadAll();
        asd.ifPresent(System.out::println);
    }

    @Autowired
    DeviceWithWeixinEntityAction action;
}