package com.legooframework.model.reactor.entity;

import com.legooframework.model.batchsupport.entity.LoginContextTest;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/reactor/spring-model-cfg.xml"}
)
public class RetailFactEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void findByParams() {
        Optional<List<RetailFactEntity>> list = retailFactEntityAction.findByParams(null);
        list.ifPresent(x -> x.forEach(y -> System.out.println(y.getCreatetime().toString("yyyy-MM-dd HH:mm"))));
    }

    @Test
    public void count() {
        OrgEntity com = orgEntityAction.loadComById(1);
        retailFactEntityAction.count4RetailSmsJob(com);
    }

    @Autowired
    private OrgEntityAction orgEntityAction;
    @Autowired
    private RetailFactEntityAction retailFactEntityAction;

}