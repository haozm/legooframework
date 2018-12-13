package com.legooframework.model.organization.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.organization.LoginContextTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/organization/spring-model-cfg.xml"}
)
public class EquipmentEntityActionTest {

    @Test
    public void findById() {
        LoginContextHolder.setCtx(new LoginContextTest());
        // 53e81ce913541c1f72695380df7d0a6c
        Optional<EquipmentEntity> opt = action.findById("A52001C69F01A8");
        Assert.assertTrue(opt.isPresent());
        System.out.println(opt.get());
    }


    @Test
    public void insert() {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Autowired
    private EquipmentEntityAction action;
}