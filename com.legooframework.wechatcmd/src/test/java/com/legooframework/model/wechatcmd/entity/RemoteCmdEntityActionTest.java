package com.legooframework.model.wechatcmd.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.wechatcircle.entity.CircleUnReadDto;
import com.legooframework.model.wechatcircle.entity.DataSourcesFrom;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/wechatcmd/spring-model-cfg.xml"}
)
public class RemoteCmdEntityActionTest {

    @Test
    public void unReadCircleCmd() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        DataSourcesFrom ds = new DataSourcesFrom("wxid_un99y5y1xzzz22", 1, 1314);
        CircleUnReadDto dto = new CircleUnReadDto(ds, Maps.newHashMap());
//        remoteCmdEntityAction.unReadCircleCmd(dto);
    }

    @Test
    public void clearUnclaimCmd() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        remoteCmdEntityAction.clearUnclaimCmd(Lists.newArrayList("xiaojie_ho"));
    }

    @Autowired
    RemoteCmdEntityAction remoteCmdEntityAction;
}