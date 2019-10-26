package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.legooframework.model.wechatcircle.entity.CircleUnReadDto;
import com.legooframework.model.wechatcircle.entity.DataSourcesFrom;
import org.apache.commons.collections4.MapUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
        ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
        ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class WechatCircleProxyActionTest {

    @Test
    public void wechatcircleUnread() {
        DataSourcesFrom ds = new DataSourcesFrom("wxid_un99y5y1xzzz22", 1, 1314);
        CircleUnReadDto dtp = new CircleUnReadDto(ds, Maps.newHashMap());
        action.wechatcircleUnread(dtp);
    }

    @Test
    public void batchSyncLastTime() {
        String weixinIds = "wxid_8drefe70em2i22,wxid_un99y5y1xzzz22,wxid_186tb5cyjmhi22";
        action.batchSyncLastTime(Splitter.on(",").splitToList(weixinIds));
    }

    @Autowired
    private WechatCircleProxyAction action;
}