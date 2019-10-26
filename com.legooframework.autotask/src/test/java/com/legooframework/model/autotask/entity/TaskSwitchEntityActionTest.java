package com.legooframework.model.autotask.entity;

import com.legooframework.model.batchsupport.entity.LoginContextTest;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
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
public class TaskSwitchEntityActionTest {


    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void add() {
        OrgEntity com = orgEntityAction.loadComById(1);
        taskSwitchEntityAction.add(com, BusinessType.HOLIDAYCARE);
    }

    @Test
    public void findSwitchesOn() {
        Optional<List<TaskSwitchEntity>> asd = taskSwitchEntityAction.findSwitchesOn();
        asd.ifPresent(x -> System.out.println(x));
    }

    @Autowired
    private OrgEntityAction orgEntityAction;
    @Autowired
    private TaskSwitchEntityAction taskSwitchEntityAction;
}