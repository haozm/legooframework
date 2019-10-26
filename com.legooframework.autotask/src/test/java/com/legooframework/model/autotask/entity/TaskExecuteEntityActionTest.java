package com.legooframework.model.autotask.entity;

import com.legooframework.model.batchsupport.entity.LoginContextTest;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronScheduleBuilder;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.spi.MutableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-autotask-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/autotask/spring-model-cfg.xml"}
)
public class TaskExecuteEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }


    @Test
    public void findById() {
        Optional<TaskExecuteEntity> exits = taskExecuteEntityAction.findById(13);
    }


    public static void main(String[] args) {
        // CronScheduleBuilder cb = (CronScheduleBuilder) CronScheduleBuilder.cronSchedule("0 23 17 15 10-10 *").build();
        // CronScheduleBuilder cb = (CronScheduleBuilder) CronScheduleBuilder.cronSchedule("").build();
        MutableTrigger cb = CronScheduleBuilder.cronSchedule("0 0 23 * * ? 2019").build();
        System.out.println(cb);
    }

    @Test
    public void findTaskExecute4Todo() {
        taskExecuteEntityAction.findTaskExecute4Todo();
    }

    @Autowired
    private TaskExecuteEntityAction taskExecuteEntityAction;
}