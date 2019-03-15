package com.csosm.module.menu.entity;

import com.google.common.collect.Sets;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class ResourcesFactoryTest {

    @Test
    public void getSubReource() {
        Optional<ResourceDto> rs = resourcesFactory.getSubReource(1L, Sets.newHashSet("page-004"));
        if (rs.isPresent())
            System.out.println(rs.get());
    }

    @Test
    public void laodRes() {
        Optional<ResourceDto> asd = resourcesFactory.getAllReource(-1L);
        System.out.println(asd.get());
    }

    @Autowired
    ResourcesFactory resourcesFactory;

}