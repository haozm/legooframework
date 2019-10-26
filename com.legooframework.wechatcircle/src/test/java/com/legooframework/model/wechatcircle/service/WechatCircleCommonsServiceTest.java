package com.legooframework.model.wechatcircle.service;

import com.google.common.collect.Lists;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-circle-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/wechatcircle/spring-model-cfg.xml"}
)
public class WechatCircleCommonsServiceTest {

    @Test
    public void loadUnReadComments() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        service.loadUnReadComments("wxid_un99y5y1xzzz22");
    }

    @Test
    public void loadAllUnReaderCount() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        service.loadUnReadComments("wxid_un99y5y1xzzz22");
    }

    @Autowired
    WechatCircleCommonsService service;
}