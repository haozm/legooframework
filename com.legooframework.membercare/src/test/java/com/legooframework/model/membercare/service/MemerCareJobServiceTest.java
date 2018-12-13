package com.legooframework.model.membercare.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.salesrecords.entity.LoginContextTest;
import org.junit.Before;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class MemerCareJobServiceTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void getTouch90Job() {
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(1);
        Preconditions.checkState(com.isPresent());
        careJobService.runTouch90JobByCompany(com.get());
    }

    @Autowired
    CrmOrganizationEntityAction organizationAction;
    @Autowired
    MemerCareJobService careJobService;

}