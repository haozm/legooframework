package com.legooframework.model.membercare.entity;

import com.legooframework.model.core.utils.WebUtils;
import org.junit.Test;

public class Touch90DetailRuleTest {

    @Test
    public void toString1() {
        //TaskCareDetailRule asd = new TaskCareDetailRule("1d", "3d",  1000L, 3000L);
       // System.out.println(asd.toString());
    }


    @Test
    public void decoding() {
        String asd = "90%E8%8A%82%E7%82%B902-%E4%BD%A0%E7%9C%BC%E5%89%8D%E5%B1%95%E5%BC%80%E4%BA%86%E4%B8%80%E5%B9%85%EF%BC%8C%E7%8B%AC%E7%89%B9%E7%9A%84%E5%8E%86%E5%8F%B2%E7%94%BB%E5%8D%B7%E3%80%82%E8%AF%97%E4%BA%BA%E9%9D%A2%E5%AF%B9%E9%95%BF%E6%B1%9F%EF%BC%8C%E9%81%A5%E6%83%B3%E5%87%A0%E7%99%BE%E5%B9%B4%E5%89%8D%E7%9A%84%E4%B8%89%E5%9B%BD%E4%BA%89%E9%9C%B8%E3%80%82%E5%85%B6%E5%90%83%E9%A5%AD%7B%E5%85%AC%E5%8F%B8%E5%90%8D%E7%A7%B0%7D%7B%E7%94%9F%E6%97%A5%E6%97%A5%E6%9C%9F%7D%7B%E4%BC%9A%E5%91%98%E7%94%B5%E8%AF%9D%7D%7B%E4%BC%9A%E5%91%98%E5%A7%93%E5%90%8D%7D%3D%3D%3D%3D%3D%3D%20%20%20%3D%20%3D%20%3D%20%E7%88%BD%E8%82%A4%E6%B0%B4";
        System.out.println(WebUtils.decodeUrl(asd));
    }
}