package com.csosm.module.web;

import com.legooframework.model.jwtoken.entity.JWTokenAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class UserDetailsServiceImplTest {
//    DelegatingFilterProxy
    @Test
    public void loadUserByUsername() {
        tokenClient.loginByWeb("1@dgax_323", "123");
    }

    @Autowired
    private JWTokenAction tokenClient;
}