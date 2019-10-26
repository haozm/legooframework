package com.legooframework.model.wechatcircle.entity;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;

import java.util.Date;

public class UnixTime {

    @Test
    public void tx() {
        long asd = 1559814094L;
        Date dt = new Date(asd * 1000);
        System.out.println(DateFormatUtils.format(dt, "yyyy-MM-dd HH:mm:ss"));
    }

}
