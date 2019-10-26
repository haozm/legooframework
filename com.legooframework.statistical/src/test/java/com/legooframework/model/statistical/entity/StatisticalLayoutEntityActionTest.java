package com.legooframework.model.statistical.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Map;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statistical/spring-model-cfg.xml"}
)
public class StatisticalLayoutEntityActionTest {

    @Before
    public void info() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void loadSubPageByCompany() {
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(12, 1);
        Optional<StatisticalLayoutEntity> asd = statisticalLayoutEntityAction.loadSubPageByUser(user, "HOMEPAGE", "saleRecord01");
        Map<String, Object> aa = asd.get().buildLayout(user, statisticalDefinedFactory);
        System.out.println(aa);
    }

    @Test
    public void loadHomePageByCompany() {
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(12, 1);
        Optional<StatisticalLayoutEntity> asd = statisticalLayoutEntityAction.loadPageByUser(user, "EMPLOYEE_SALES");
        Map<String, Object> aa = asd.get().buildLayout(user, statisticalDefinedFactory);
        System.out.println(aa);
    }

    @Autowired
    private StatisticalDefinedFactory statisticalDefinedFactory;
    @Autowired
    private UserAuthorEntityAction userAuthorEntityAction;

    @Autowired
    private StatisticalLayoutEntityAction statisticalLayoutEntityAction;
}