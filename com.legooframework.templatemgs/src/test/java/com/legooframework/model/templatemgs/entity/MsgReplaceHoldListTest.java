package com.legooframework.model.templatemgs.entity;

import com.google.common.collect.Maps;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.junit.Test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgReplaceHoldListTest {


    public void repalce() {
    }

    @Test
    public void checkTemplate() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("你好", "niohao");
        params.put("门店领导", "324324");
        StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
        String temp = "{你 好}会员姓名{门店领导}回家{休息:-睡觉}";
        System.out.println(substitutor.replace(temp));
    }

    @Test
    public void checkTemplate2() {
        String regEx = "(?<=\\{)[^}]*(?=})";
        String temp = "{ 你 好 }会员姓名{门店领导}";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(temp);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}