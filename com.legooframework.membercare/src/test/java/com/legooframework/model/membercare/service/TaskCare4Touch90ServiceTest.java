package com.legooframework.model.membercare.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.membercare.entity.TaskSwitchEntity;
import com.legooframework.model.membercare.entity.TaskSwitchEntityAction;
import com.legooframework.model.salesrecords.entity.LoginContextTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.job.SimpleStepHandler;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-membercare-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class TaskCare4Touch90ServiceTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void runTouch90JobByCompany() {
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(100098);
        Preconditions.checkState(com.isPresent());
        taskCare4Touch90Service.runTouch90JobByCompany(com.get());
    }

    @Test
    public void autoRunTouch90Jobs() {
        taskCare4Touch90Service.autoRunTouch90Jobs();
    }

    @Test
    public void run90() {
        taskCare4Touch90Service.runTouch90Job();
    }

    @Test
    public void automaticStartingTask() {
        careJobService.automaticStartingTask();
    }

    @Test
    public void automaticExtensionedTask() {
        careJobService.automaticExtensionedTask();
    }

    @Test
    public void automaticExpiredTask() {
        careJobService.automaticExpiredTask();
    }

    @Test
    public void canceledByTask() {
        //careJobService.canceledByTask(506221869);
    }

    @Autowired
    MemberCareJobService careJobService;
    @Autowired
    CrmOrganizationEntityAction organizationAction;
    @Autowired
    TaskCare4Touch90Service taskCare4Touch90Service;
    @Autowired
    TaskSwitchEntityAction taskSwitchEntityAction;

}