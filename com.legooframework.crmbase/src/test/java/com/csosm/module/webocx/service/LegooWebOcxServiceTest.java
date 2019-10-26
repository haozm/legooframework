package com.csosm.module.webocx.service;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.BaseModelServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/jwtoken/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class LegooWebOcxServiceTest {

    @Test
    public void statisticalByGroup() {
    }

    @Test
    public void loadByGroupId() {
    }

    @Test
    public void loadOcxById() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx050");
        webOcxService.loadOcxById("memberDetail", null, user);
    }

    @Autowired
    private BaseModelServer baseAction;
    @Autowired
    LegooWebOcxService webOcxService;
}