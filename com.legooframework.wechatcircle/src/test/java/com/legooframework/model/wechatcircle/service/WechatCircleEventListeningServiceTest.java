package com.legooframework.model.wechatcircle.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.wechatcircle.entity.DataSourcesFrom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-circle-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/wechatcircle/spring-model-cfg.xml"}
)
public class WechatCircleEventListeningServiceTest {

    @Test
    public void listeningEvent() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        DataSourcesFrom ds = new DataSourcesFrom("wxid_un99y5y1xzzz22", 1, 1314);
//        WechatCircleEvent event = WechatCircleEvent.createUnReadCmtsEvent(ds);
//        listeningService.getMessagingTemplate().send("wechatCircleEventBus", event.toMessage(LoginContextHolder.get()));
    }


}