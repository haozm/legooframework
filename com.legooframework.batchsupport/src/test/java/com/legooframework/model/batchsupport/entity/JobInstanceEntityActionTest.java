package com.legooframework.model.batchsupport.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml"}
)
public class JobInstanceEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void loadAllJob() {
        List<String> asd = jobInstanceEntityAction.loadAllJobInstance();
        System.out.println(asd);
    }

    @Test
    public void getLastJobExecution() {
        Optional<JobExecution> opt = jobInstanceEntityAction.getLastJobExecution("touch90Job", 2);
        System.out.println(opt.isPresent());
        opt.ifPresent(System.out::println);
    }

    @Test
    public void getLastJobInstance() {
        Optional<JobInstance> opt = jobInstanceEntityAction.getLastJobInstance("touch90Job", 1);
        System.out.println(opt.isPresent());
        opt.ifPresent(System.out::println);
    }

    @Autowired
    JobInstanceEntityAction jobInstanceEntityAction;
}