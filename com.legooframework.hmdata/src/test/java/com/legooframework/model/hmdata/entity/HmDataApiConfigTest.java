package com.legooframework.model.hmdata.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/hmdata/spring-model-cfg.xml"
        }
)
public class HmDataApiConfigTest {

    @Test
    public void getPrivateKey() {
        System.out.println(hmDataApiConfig.getPrivateKey());
    }

    @Test
    public void getPublicKey() {
        System.out.println(hmDataApiConfig.getPublicKey());
    }


    @Autowired
    private HmDataApiConfig hmDataApiConfig;
}