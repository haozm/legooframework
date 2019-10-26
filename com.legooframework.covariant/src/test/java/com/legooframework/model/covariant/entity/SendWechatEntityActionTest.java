package com.legooframework.model.covariant.entity;

import com.google.common.collect.Sets;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class SendWechatEntityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }


    @Test
    public void sendMsg() {
        StoEntity store = stoEntityAction.loadById(1120);
        sendWechatEntityAction.sendMsg("n你妈妈含义回家吃饭", null, Sets.newHashSet("xiaojie_hao"),
                store, BusinessType.BIRTHDAYCARE);
    }

    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private SendWechatEntityAction sendWechatEntityAction;
}