package com.legooframework.model.regiscenter.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/regiscenter/spring-model-cfg.xml"}
)
public class StoreActiveInfoEntityActionTest {

    @Test
    public void findByDeviceId() {
        LoginContextHolder.setCtx(new LoginContextTest());
//        Optional<CrmOrganizationEntity> com = organizationEntityAction.findCompanyById(1);
//        Optional<CrmStoreEntity> stor = storeEntityAction.findById(com.get(), 10);
        Optional<StoreActiveInfoEntity> asd = storeActiveInfoEntityAction.findByDeviceId("123");
        asd.ifPresent(System.out::println);
    }

    @Test
    public void findByStore() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<CrmOrganizationEntity> com = organizationEntityAction.findCompanyById(1);
        Optional<CrmStoreEntity> stor = storeEntityAction.findById(com.get(), 10);
        Optional<List<StoreActiveInfoEntity>> asd = storeActiveInfoEntityAction.findByStore(stor.get());
        asd.ifPresent(System.out::println);
    }

    @Test
    public void activeDevice() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<CrmOrganizationEntity> com = organizationEntityAction.findCompanyById(1);
        Optional<CrmStoreEntity> stor = storeEntityAction.findById(com.get(), 11);
        storeActiveInfoEntityAction.activeDevice(stor.get(), "123123123");
    }

    @Test
    public void changeDevice() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<CrmOrganizationEntity> com = organizationEntityAction.findCompanyById(1);
        Optional<CrmStoreEntity> stor = storeEntityAction.findById(com.get(), 11);
        // storeActiveInfoEntityAction.changeDevice(stor.get(), "asdasdasdasd", "123123");
    }

    @Autowired
    CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    CrmStoreEntityAction storeEntityAction;
    @Autowired
    private StoreActiveInfoEntityAction storeActiveInfoEntityAction;
}