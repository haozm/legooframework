package com.legooframework.model.membercare.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-membercare-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class UpcomingTaskDetailEntityActionTest {

    @Test
    public void loadByTask() {
        LoginContextHolder.setAnonymousCtx();
        Optional<UpcomingTaskEntity> asd = taskEntityAction.findById("0073a731-2284-4a06-8548-96508c10459f");
        detailEntityAction.initByTask(asd.get());
    }

    @Test
    public void loadDetailById() {
        LoginContextHolder.setAnonymousCtx();
        detailEntityAction.extensionedDetails();
    }

    @Test
    public void loadAutoRunJobDetails() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<UpcomingTaskDetailEntity>> details = detailEntityAction.loadAutoRunJobDetails(BusinessType.TOUCHED90);
        details.ifPresent(System.out::println);
    }

    @Autowired
    UpcomingTaskDetailEntityAction detailEntityAction;
    @Autowired
    UpcomingTaskEntityAction taskEntityAction;

    @Test
    public void expiredDetails() {
        LoginContextHolder.setAnonymousCtx();
        detailEntityAction.expiredDetails();
    }
}