package com.legooframework.model.wechatcircle.service;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.legooframework.model.wechatcircle.entity.WechatCircleEntity;
import com.legooframework.model.wechatcircle.entity.WechatCircleTranDto;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ProtocolCodingFactoryTest {

    Gson gosn = new Gson();

    @Test
    public void deCodingCircle() {
        Map<String, Object> soft = Maps.newHashMap();
        soft.put("field_likeFlag", 0);
        soft.put("field_snsId", 128970237843284324L);
        soft.put("imageCount", 0);
        soft.put("snsId", "13088079975939715258");
        soft.put("snsMessage", "用原木艺术，致敬时代情怀，探索东方之美，听");
        soft.put("snsTime", 1560220715);
        soft.put("snsType", 3);
        soft.put("snsUserName", "asd3366117");
        Map<String, Object> linksEntryList = Maps.newHashMap();
        linksEntryList.put("Id", "13088079976604774553");
        linksEntryList.put("desc", "用原木艺术，致敬时代情怀，探索东方之美，听木头的故事");
        linksEntryList.put("imgUrl", "http://szmmsns.qpic.cn/mmsns/vgwOZPicMe8LeRKpQfnS5yGA65eia3sReg6vKVh5Ee36BpX9s32PuWNotTcLawzYZrJ9K1dzWy2mU/150");
        linksEntryList.put("title", "木有故事.耳机王子/蓝牙音响");
        linksEntryList.put("url", "https://z.m.jd.com/project/newDetails/114021.html");
        soft.put("linksEntryList", linksEntryList);
        String json = gosn.toJson(new Map[]{soft});
        List<WechatCircleTranDto> asd= ProtocolCodingFactory.deCodingCircle("xioaojie.hao", 100098, 1315, json);
        System.out.println(asd);
    }
}