package com.legooframework.model.statistical.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statistical/spring-model-cfg.xml"}
)
public class StatisticalRefEntityActionTest {

    @Before
    public void info() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findByCompany() {
        statisticalRefEntityAction.findByCompany(null);
    }


    @Autowired
    private UserAuthorEntityAction userAuthorEntityAction;
    @Autowired
    private StatisticalRefEntityAction statisticalRefEntityAction;
}