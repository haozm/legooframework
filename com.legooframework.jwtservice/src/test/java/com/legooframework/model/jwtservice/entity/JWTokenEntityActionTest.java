package com.legooframework.model.jwtservice.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/jwtservice/spring-model-cfg.xml"}
)
public class JWTokenEntityActionTest {

    @Test
    public void loadJWToken() {
        LoginContextHolder.setAnonymousCtx();
        action.loadJWToken("asdasd");
    }

    @Test
    public void loginByWeb() {
        LoginContextHolder.setAnonymousCtx();
        action.loginByWeb("dgqx311_1", "127.0.0.1");
    }

    @Test
    public void findById() {
        LoginContextHolder.setAnonymousCtx();
        Optional<JWTokenEntity> as = action.findById("de399772-518f-4a84-b34c-2795a936ff9d");
        action.logout(as.get().getLoginToken());
    }

    @Test
    public void loginByMobile() {
        LoginContextHolder.setAnonymousCtx();
        action.loginByMobile("dgqx311_1", "127.0.0.1");
    }


    @Autowired
    JWTokenEntityAction action;
}