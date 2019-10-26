package com.legooframework.model.wechatcircle.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.wechatcircle.service.ProtocolCodingFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-circle-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/wechatcircle/spring-model-cfg.xml"}
)
public class WechatCircleEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
    }

    @Test
    public void saveOrUpdate() {
//        DataSourcesFrom sour = new DataSourcesFrom("weixin_hao12312312", 100098, 1315);
//        WechatCircleEntity txt = WechatCircleEntity.textContent("weixin_hao", 111L, "circle_id_123", "这是一套坪林", 16123123L, sour);
//        action.saveOrUpdate(txt, sour);
    }

    @Test
    public void findbyids() {
        action.findById(-5352740767996432308L, "wxid_10le2rvuqfcy22");

    }


    @Test
    public void addImage() {
        Long id = -5375729206025899893L;
        String wexinId = "wxid_9228942288022";
        String images_json = "[{\"url\":\"http://anlink.cdn.csosm.com/76477CA261AA002241A838842D2BE42229A4E9CDAA082E900CF04ECFCB4FBFCF006F1A4899EAC30A533E38E6E09AB8F3.jpg\",\"Id\":\"13079747633712541806\"}]";
        JsonParser parser = new JsonParser();
        JsonArray json_all_array = parser.parse(images_json).getAsJsonArray();
        List<WechatCircleImage> images_ctx = Lists.newArrayList();
        json_all_array.forEach(json_img -> ProtocolCodingFactory.deCodingImage(json_img.getAsJsonObject()).ifPresent(images_ctx::add));
        System.out.println(images_ctx.size());
        action.addImages(id, wexinId, images_ctx);
    }

    @Test
    public void saveOrUpdateMix() {
        DataSourcesFrom sour = new DataSourcesFrom("wiasdasd", 100098, 1315);
        List<WechatCircleImage> imgs = Lists.newArrayList();
        String[] ids = new String[]{"001", "002", "003", "004", "006", "007", "0123", "0123123", "01435", "034543", "07234", "0541231"};
        for (int i = 0; i < 5; i++) {
            imgs.add(new WechatCircleImage(ids[i], "ing", "imgs2", i));
        }
//        WechatCircleEntity txt = WechatCircleEntity.mixContent("weixin_Idasdasd", 11123123123L, "asdasdasd", "asdasdasdasd", 6, imgs,
//                12312312313L, sour);
        //  action.saveOrUpdate(txt, sour);
    }

    @Autowired
    private WechatCircleEntityAction action;
}

