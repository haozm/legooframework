package com.legooframework.model.takecare.service;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.SendChannel;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/takecare/spring-model-cfg.xml"}
)
public class TakeCareServiceTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void batchBirthdayCare() {
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(89, null);
        careService.batchBirthdayCare(Lists.newArrayList(344, 345, 346, 347, 348, 349, 350, 351, 352,
                353), Lists.newArrayList(SendChannel.SMS, SendChannel.WECHAT), null, null, user);
    }

    @Autowired
    private UserAuthorEntityAction userAuthorEntityAction;
    @Autowired
    private TakeCareService careService;
}