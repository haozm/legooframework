package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeDetailEntityAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SMSRechargeServiceTest {

    @Test
    public void rechargeByCompany() {
        LoginContextHolder.setAnonymousCtx();
        List<Integer> out = Lists.newArrayList(30, 31, 35);
//        String res = rechargeService.rechargeByCompany(1, out, 500000);
//        System.out.println(res);
    }

    @Test
    public void rechargeByOrg() {
        LoginContextHolder.setAnonymousCtx();
        List<Integer> in = Lists.newArrayList(3, 4, 5);
        List<Integer> out = Lists.newArrayList(30, 31, 35);
//        String res = rechargeService.rechargeByOrg(1, 7, null, out, 600000);
//        System.out.println(res);
    }

    @Test
    public void rechargeByStore() {
        LoginContextHolder.setAnonymousCtx();
//        String res = rechargeService.rechargeByStore(1, 17, 600000);
//        System.out.println(res);
    }

    @Test
    public void rechargeByStoreOnce() {
    }

    @Test
    public void rechargeByCompanyOnce() {
        LoginContextHolder.setAnonymousCtx();
//        String res = rechargeService.rechargeByCompanyOnce(1, null, 600000, 5.5, "测试充值公司一次性");
//        System.out.println(res);
    }

    @Autowired
    CrmOrganizationEntityAction companyAction;
    @Autowired
    CrmStoreEntityAction storeAction;
    @Autowired
    RechargeDetailEntityAction rechargeDetailEntityAction;
    @Autowired
    private SMSRechargeService rechargeService;
}