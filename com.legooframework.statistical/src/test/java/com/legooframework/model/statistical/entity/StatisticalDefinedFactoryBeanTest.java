package com.legooframework.model.statistical.entity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statistical/spring-model-cfg.xml"}
)
public class StatisticalDefinedFactoryBeanTest {

    @Test
    public void createInstance() {
        Assert.assertNotNull(statisticalDefinedFactory);
        Optional<StatisticalEntity> as = statisticalDefinedFactory.findById("saleRecord01");
        System.out.println(as);
    }

    @Autowired
    private StatisticalDefinedFactory statisticalDefinedFactory;
}