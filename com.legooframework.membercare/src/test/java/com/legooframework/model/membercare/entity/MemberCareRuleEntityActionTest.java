package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.salesrecords.entity.LoginContextTest;
import org.junit.Before;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class MemberCareRuleEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void loadTouch90Rules() {
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(1);
        Preconditions.checkState(com.isPresent());
        Optional<CrmStoreEntity> store = storeEntityAction.findById(com.get(), 8);
        Preconditions.checkState(store.isPresent());
        Optional<List<Touch90CareRuleEntity>> asd=  careRuleEntityAction.loadAllTouch90Rules(com.get());
        System.out.println(asd.isPresent());
    }

    @Test
    public void saveOrUpdate90Rule() {
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(1);
        Preconditions.checkState(com.isPresent());
        Optional<CrmStoreEntity> store = storeEntityAction.findById(com.get(), 8);
        Preconditions.checkState(store.isPresent());
        //careRuleEntityAction.saveOrUpdate90Rule(com.get(), store.get(), true, true, 30, 1500);
    }

    @Autowired
    CareRuleEntityAction careRuleEntityAction;
    @Autowired
    CrmOrganizationEntityAction organizationAction;
    @Autowired
    CrmStoreEntityAction storeEntityAction;
}