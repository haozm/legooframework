package com.legooframework.model.smsprovider.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-smsprovider-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/entities/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsprovider/spring-model-cfg.xml"}
)
public class SMSProviderEntityActionTest {

    @Test
    public void loadAllSubAccounts() {
        LoginContextHolder.setAnonymousCtx();
        List<SMSSubAccountEntity> res = smsSubAccountEntityAction.loadAllSubAccounts();
        System.out.println(res);
        System.out.println(smsSubAccountEntityAction.loadSubAccountByAccount("yiyuanxx"));
        System.out.println(smsSubAccountEntityAction.loadSubAccountByAccount("yiyuanxxyx"));
    }

    @Test
    public void loadAllProviders() {
        LoginContextHolder.setAnonymousCtx();
        List<SMSProviderEntity> res = smsSubAccountEntityAction.loadAllProviders();
    }

    @Autowired
    SMSProviderEntityAction smsSubAccountEntityAction;
}