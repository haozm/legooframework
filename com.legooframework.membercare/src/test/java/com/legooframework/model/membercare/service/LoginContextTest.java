package com.legooframework.model.membercare.service;

import com.legooframework.model.core.base.runtime.AnonymousCtx;
import org.junit.Test;
import org.springframework.util.NumberUtils;

public class LoginContextTest extends AnonymousCtx {

    public LoginContextTest() {
        super(100000000L, "admin", 100000000L);
    }

    @Test
    public void stie() {
        String asd= "123123213.0";
        System.out.println(Integer.valueOf(asd));
        //System.out.println(new Integer("123213.0"));
    }
}
