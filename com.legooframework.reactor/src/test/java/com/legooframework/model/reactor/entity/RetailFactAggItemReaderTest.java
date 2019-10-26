package com.legooframework.model.reactor.entity;

import com.legooframework.model.batchsupport.entity.LoginContextTest;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/reactor/spring-model-cfg.xml"}
)
public class RetailFactAggItemReaderTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void getSql() {
        System.out.println(retailFactAggItemReader.getSql());
    }

    @Test
    public void reader() throws Exception {
        int counter = 0;
        ExecutionContext executionContext = new ExecutionContext();
        retailFactAggItemReader.open(executionContext);
        Object customerCredit = new Object();
        while (customerCredit != null) {
            customerCredit = retailFactAggItemReader.read();
            counter++;
        }
        System.out.println(counter);
        retailFactAggItemReader.cleanupOnClose();
    }

    @Autowired
    RetailFactAggItemReader retailFactAggItemReader;

}