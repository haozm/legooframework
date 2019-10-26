package com.legooframework.model.autotask.entity;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-autotask-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/autotask/spring-restful-cfg.xml"}
)
public class RestTemplateHelperTest {

    @Test
    public void postNoPathVariables() {
        String url = "";
        Map<String, Object> params = Maps.newHashMap();
        restTemplateHelper.postNoPathVariables(url, params);
    }

    @Autowired
    private RestTemplateHelper restTemplateHelper;
}