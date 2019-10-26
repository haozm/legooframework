package com.legooframework.model.wechatcircle.entity;

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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-circle-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/wechatcircle/spring-model-cfg.xml"}
)
public class CirclePermissionEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
    }

    @Test
    public void saveOrUpdate() {
//        action.saveOrUpdate("wexi_asd", 2001, null);
//        action.saveOrUpdate("wexi_asd", 2001, null);
    }

    @Autowired
    CirclePermissionEntityAction action;
}