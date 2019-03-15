package com.legooframework.model.regiscenter.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/regiscenter/spring-model-cfg.xml"}
)
public class DevicePinCodeEntityActionTest {

    //    public boolean activeDeviceId(String pinCode, String deviceId)
    @Test
    public void activeDeviceId() {
        LoginContextHolder.setCtx(new LoginContextTest());
        //boolean fa = devicePinCodeEntityAction.activeDeviceId("162008", "VIP_092823123");
        //  System.out.println(fa);
    }

    @Test
    public void activeDevice() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<CrmOrganizationEntity> com = companyEntityAction.findCompanyById(1);
        // 34
        Optional<CrmStoreEntity> store = storeAction.findById(com.get(), 34);
        devicePinCodeEntityAction.activeDevice("842143", "090-90909379487324", store.get());
    }

    @Test
    public void findByCode() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<DevicePinCodeEntity> asd = devicePinCodeEntityAction.findByCode("344107");
        Assert.assertTrue(asd.isPresent());
        System.out.println(asd.get().getStauts());
    }

    @Test
    public void findByCodeOrDeviceId() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<List<DevicePinCodeEntity>> asd = devicePinCodeEntityAction.findByCodeOrDeviceId("216012", "VIP_092823123");
        Assert.assertTrue(asd.isPresent());
    }

    @Test
    public void batchCreatePinCodes() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<CrmOrganizationEntity> com = companyEntityAction.findCompanyById(1);
        devicePinCodeEntityAction.batchCreatePinCodes(com.get(), 4);
    }

    @Test
    public void page() {
        LoginContextHolder.setCtx(new LoginContextTest());
        PagingResult pagingResult = jdbcQuerySupport.queryForPage("DevicePinCodeEntity", "loadPincode", 1, 20,
                null);
        System.out.println(pagingResult.toData());
    }

    @Autowired
    @Qualifier(value = "regiscenterJdbcQuery")
    private JdbcQuerySupport jdbcQuerySupport;

    @Autowired
    CrmStoreEntityAction storeAction;
    @Autowired
    CrmOrganizationEntityAction companyEntityAction;
    @Autowired
    DevicePinCodeEntityAction devicePinCodeEntityAction;

}