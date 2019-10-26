package com.legooframework.model.rfm.entity;

import com.csosm.module.base.entity.MemberEntityAction;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/rfm/spring-model-cfg.xml"}
)
public class MemberRFMEntityActionTest {

    @Test
    public void findCurMember() {
        Optional<MemberRFMEntity> asd = rfmEntityAction.findByMember(1, 10);
        asd.ifPresent(System.out::println);
    }

    @Test
    public void findAllMember() {
        Optional<List<MemberRFMEntity>> asd = rfmEntityAction.findAllByMember(1, 345);
        asd.ifPresent(System.out::println);
    }

    @Autowired
    MemberEntityAction memberEntityAction;
    @Autowired
    MemberRFMEntityAction rfmEntityAction;
}