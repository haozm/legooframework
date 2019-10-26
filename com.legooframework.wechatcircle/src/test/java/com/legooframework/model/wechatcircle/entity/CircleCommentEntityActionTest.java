package com.legooframework.model.wechatcircle.entity;

import com.google.common.collect.Lists;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-circle-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/wechatcircle/spring-model-cfg.xml"}
)
public class CircleCommentEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
    }


    @Test
    public void loadUnReadComments() {
        Optional<List<CircleCommentEntity>> list_c = commentEntityAction.loadUnReadComments("wxid_un99y5y1xzzz22");
        if (list_c.isPresent()) {
            Set<Long> ids = list_c.get().stream().map(CircleCommentEntity::getCircleId).collect(Collectors.toSet());
            circleEntityAction.findByCircleIds(ids);
        }
    }

    @Test
    public void saveOrUpdate() {
        DataSourcesFrom sourceFrom = new DataSourcesFrom("wiasdasd", 100098, 1315);
        // gh_1cf0ed52e419	13078178674657859420
        Optional<WechatCircleEntity> wechatCircle = circleEntityAction.findById(-5368565399051692196L, "gh_1cf0ed52e419");
        List<CircleCommentEntity> commentEntities = Lists.newArrayList();
        for (int i = 1; i < 2; i++) {
            commentEntities.add(new CircleCommentEntity(i, wechatCircle.get().getWeixinId(), wechatCircle.get().getId(),
                    1233455666L, String.format("%s_ii", i),
                    String.format("nicheng_%s", i), true, sourceFrom));
        }
        commentEntityAction.saveOrUpdate(wechatCircle.get(), commentEntities, sourceFrom);
    }

    @Test
    public void findByWechatCircles() {
//        -5360171465160060817	wxid_idttwadadurb22
//                -5360149510972952486	wxid_pos30d4g9mka12
        Optional<WechatCircleEntity> optional = circleEntityAction.findById(-5351951395570765663L, "wxid_qgtejec22fie21");
        List<WechatCircleEntity> ls = Lists.newArrayList(optional.get());
        Optional<List<CircleCommentEntity>> asd = commentEntityAction.findByWechatCircles(ls, "wxid_n6b113ksfwx422");
        System.out.println("end");
    }


    @Test
    public void findByIds() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", 1);
        params.put("weixinId", "gh_1cf0ed52e419");
        params.put("circleId", -5368565399051692196L);
        commentEntityAction.findByIds(Lists.newArrayList(params));
    }

    @Autowired
    private WechatCircleEntityAction circleEntityAction;
    @Autowired
    private CircleCommentEntityAction commentEntityAction;

}