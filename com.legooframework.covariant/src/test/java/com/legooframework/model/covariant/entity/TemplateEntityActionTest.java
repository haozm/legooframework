package com.legooframework.model.covariant.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class TemplateEntityActionTest {
    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findAll() {
        Optional<List<TemplateEntity>> asd = templateEntityAction.findAll();
        asd.ifPresent(c -> System.out.println(c.size()));
    }


    @Test
    public void findByStoreWithClassifies() {
        StoEntity store = stoEntityAction.loadById(1120);
        Optional<TemplateEntity> sa = templateEntityAction.findByStoreWithClassifies(store, TemplateEntity.CLASSIFIES_RIGHTS_AND_INTERESTS);
        TemplateEntity temp = sa.get();
        Map<String, Object> params = Maps.newHashMap();
        params.put("消费金额", 1212.00);
        params.put("消费积分", 100.00);
        System.out.println(temp.replace(params));
    }

    @Autowired
    private StoEntityAction stoEntityAction;

    @Autowired
    private TemplateEntityAction templateEntityAction;
}