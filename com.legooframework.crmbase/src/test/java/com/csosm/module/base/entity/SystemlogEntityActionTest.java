package com.csosm.module.base.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class SystemlogEntityActionTest {

    @Test
    public void insert() {
        systemlogEntityAction.insert(SystemlogEntity.update(this.getClass(), "main", "asdasdasd", "test"));
    }

    @Autowired
    SystemlogEntityAction systemlogEntityAction;
}