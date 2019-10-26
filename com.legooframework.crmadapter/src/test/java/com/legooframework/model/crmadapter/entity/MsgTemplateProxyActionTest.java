package com.legooframework.model.crmadapter.entity;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class MsgTemplateProxyActionTest {

    @Test
    public void readDefTemplateByClassfies() {
        templateProxyAction.readDefTemplateByClassfies(Lists.newArrayList("TOUCH90_100098_0_15,TOUCH90_100098_0_90"));
    }

    @Autowired
    private MsgTemplateProxyAction templateProxyAction;
}