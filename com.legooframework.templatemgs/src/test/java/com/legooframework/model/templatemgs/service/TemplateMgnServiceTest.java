package com.legooframework.model.templatemgs.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.TreeNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db2-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/templatemgs/spring-model-cfg.xml"}
)
public class TemplateMgnServiceTest {

    @Test
    public void loadTreeNodeByCompany() {
        LoginContextHolder.setAnonymousCtx();
        TreeNode tr = templateMgnService.loadTreeNodeByCompany(1);
        System.out.println(tr);
    }

    @Autowired
    TemplateMgnService templateMgnService;
}