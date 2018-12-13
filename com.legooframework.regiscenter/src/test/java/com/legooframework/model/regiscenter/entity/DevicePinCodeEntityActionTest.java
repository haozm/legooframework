package com.legooframework.model.regiscenter.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.organization.entity.CompanyEntity;
import com.legooframework.model.organization.entity.CompanyEntityAction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/organization/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/regiscenter/spring-model-cfg.xml"}
)
public class DevicePinCodeEntityActionTest {

    //    public boolean activeDeviceId(String pinCode, String deviceId)
    @Test
    public void activeDeviceId() {
        LoginContextHolder.setCtx(new LoginContextTest());
        boolean fa = devicePinCodeEntityAction.activeDeviceId("162008", "VIP_092823123");
        System.out.println(fa);
    }

    @Test
    public void batchCreatePinCodes() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<CompanyEntity> com = companyEntityAction.findById(100000000L);
        Assert.assertTrue(com.isPresent());
        devicePinCodeEntityAction.batchCreatePinCodes(com.get(), null, 20);
    }

    @Test
    public void findByCode() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<DevicePinCodeEntity> asd = devicePinCodeEntityAction.findByCode("216012");
        Assert.assertTrue(asd.isPresent());
    }

    @Test
    public void findByCodeOrDeviceId() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<List<DevicePinCodeEntity>> asd = devicePinCodeEntityAction.findByCodeOrDeviceId("216012", "VIP_092823123");
        Assert.assertTrue(asd.isPresent());
    }

    @Test
    public void findByDeviceId() {
    }

    @Autowired
    CompanyEntityAction companyEntityAction;
    @Autowired
    DevicePinCodeEntityAction devicePinCodeEntityAction;
}