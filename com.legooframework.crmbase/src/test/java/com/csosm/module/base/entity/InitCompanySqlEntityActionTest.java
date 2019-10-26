package com.csosm.module.base.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-crm-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class InitCompanySqlEntityActionTest {

    @Test
    public void parseSqlFile() throws Exception {
       // companySqlEntityAction.initCompany(877);
    }

    @Autowired
    private InitCompanySqlEntityAction companySqlEntityAction;
}