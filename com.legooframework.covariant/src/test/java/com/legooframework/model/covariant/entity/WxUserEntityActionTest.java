package com.legooframework.model.covariant.entity;

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
public class WxUserEntityActionTest {


    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findById() {
        StoEntity store = stoEntityAction.loadById(1117);
        wxUserEntityAction.findById(store, "wxid_j4mzjuilduuz11");
    }


    @Autowired
    StoEntityAction stoEntityAction;
    @Autowired
    WxUserEntityAction wxUserEntityAction;
}