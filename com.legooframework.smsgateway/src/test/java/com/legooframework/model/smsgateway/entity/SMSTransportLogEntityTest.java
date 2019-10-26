package com.legooframework.model.smsgateway.entity;

import com.google.common.escape.UnicodeEscaper;
import com.google.common.html.HtmlEscapers;
import org.apache.commons.codec.net.URLCodec;
import org.junit.Test;

import static org.junit.Assert.*;

public class SMSTransportLogEntityTest {

    @Test
    public void getSms() throws Exception {
        String asd = "asjhd%<>lasdj你好 啊是对";
        System.out.println(HtmlEscapers.htmlEscaper().escape(asd));
//        Urlencode
        URLCodec codec = new URLCodec("UTF-8");
        System.out.println(codec.encode("daa971e7d9de49354f0d008c00ef9d66&"));
    }
}