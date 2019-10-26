package com.legooframework.model.covariant.service;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.BusinessType;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class CovariantServiceTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void loadMemberAgg() {
        covariantService.loadMemberAgg(45);
    }


    @Test
    public void preSendSmsByStore() {
        covariantService.preSendSmsByStore(1120, 389255, Lists.newArrayList(4249, 4250, 4251, 4252),
                BusinessType.BIRTHDAYCARE, "{会员姓名}你好");
    }

    @Test
    public void loadStoreAgg() {
        covariantService.loadStoreAgg(1120);
        System.out.println("------------------------");
        covariantService.loadStoreAgg(1120);
    }

    @Autowired
    CovariantService covariantService;
}