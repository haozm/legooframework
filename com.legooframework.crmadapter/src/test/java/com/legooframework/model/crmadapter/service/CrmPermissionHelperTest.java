package com.legooframework.model.crmadapter.service;

import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class CrmPermissionHelperTest {

    @Test
    public void authentication() {
        LoginUser user = crmReadService.loadByLoginName(999, "zongjingli");
//        Authenticationor authenticat = crmPermissionHelper.authentication(user);
//        System.out.println(authenticat);
    }

    @Test
    public void authentication2() {
        LoginUser user = crmReadService.loadByLoginName(999, "zongjingli");
//        Authenticationor authenticat = crmPermissionHelper.authentication(user, "1315,1316,1317");
//        System.out.println(authenticat);
    }

    @Test
    public void authentication3() {
        LoginUser user = crmReadService.loadByLoginName(999, "zongjingli");
//        Authenticationor authenticat = crmPermissionHelper.authentication(user, "1315.0");
//        System.out.println(authenticat);
    }

    @Autowired
    private CrmReadService crmReadService;
    @Autowired
    private CrmPermissionHelper crmPermissionHelper;
}