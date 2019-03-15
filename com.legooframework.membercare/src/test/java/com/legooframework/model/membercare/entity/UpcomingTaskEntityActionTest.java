package com.legooframework.model.membercare.entity;

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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class UpcomingTaskEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void groupByMemberTouchFrist() {
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(1);
        Preconditions.checkState(com.isPresent());
//        Optional<List<UpcomingTaskEntity>> as = upcomingTaskAction.groupByMemberTouchFrist(com.get(), null);
//        System.out.println(as.isPresent());
    }

    @Test
    public void loadDetailById() {
        upcomingTaskAction.loadTouch90DetailByStauts(TaskStatus.Create);
//        Optional<List<UpcomingTaskEntity>> as = upcomingTaskAction.groupByMemberTouchFrist(com.get(), null);
//        System.out.println(as.isPresent());
    }

    @Test
    public void findById() {
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(1);
        Preconditions.checkState(com.isPresent());
    }

    @Autowired
    CrmOrganizationEntityAction organizationAction;
    @Autowired
    UpcomingTaskEntityAction upcomingTaskAction;
}