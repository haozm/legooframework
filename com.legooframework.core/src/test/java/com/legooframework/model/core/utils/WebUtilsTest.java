package com.legooframework.model.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class WebUtilsTest {

    @Test
    public void encodeUrl() {
        System.out.println(WebUtils.encodeUrl("也暗示{门店名称}本世代{会员姓名}你好"));
    }

    @Test
    public void asd() {
        String result_payload = "5318ced6-b2d5-4c24-ac4c-c6e997434116|0000|OK||6a0746df-dece-48f7-9de2-dafbdd20d90a|0000|OK||700a96bf-4e3f-4194-9920-7c9341bf69dd|0000|OK||c8d5047f-4bf6-4a09-9cc8-df99f9a32be4|0000|OK";
        String[] aaa = StringUtils.splitByWholeSeparator(result_payload, "||");
        System.out.println(aaa.length);
    }

    @Test
    public void decodeUrl() {
    }
}