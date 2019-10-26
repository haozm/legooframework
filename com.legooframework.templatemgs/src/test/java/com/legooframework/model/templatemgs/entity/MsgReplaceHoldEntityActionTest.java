package com.legooframework.model.templatemgs.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.crmadapter.service.CrmReadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db2-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/templatemgs/spring-model-cfg.xml"}
)
public class MsgReplaceHoldEntityActionTest {

    @Test
    public void loadByUser() {
        LoginUser user = crmReadService.loadByLoginName(1, "00006HBDYMTJ");
        LoginContextHolder.setCtx(user);
        replaceHoldEntityAction.loadByUser(user);
    }

    @Autowired
    CrmReadService crmReadService;
    @Autowired
    MsgReplaceHoldEntityAction replaceHoldEntityAction;
}