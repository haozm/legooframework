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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-membercare-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class TaskCareRuleEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void loadTouch90Rules() {
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(100098);
        Preconditions.checkState(com.isPresent());
        Optional<CrmStoreEntity> store = storeEntityAction.findById(com.get(), 1315);
        Preconditions.checkState(store.isPresent());
//        Optional<TaskCareRule4Touch90Entity> list = careRuleEntityAction.loadTouch90RuleByStore(store.get(), null);
//        list.ifPresent(x -> System.out.println(x));
//
//        Optional<List<TaskCareRule4Touch90Entity>> ruleIds = careRuleEntityAction.loadAllTouch90RuleByCompany(com.get());
//        ruleIds.ifPresent(x -> System.out.println(x));
    }

    @Test
    public void saveOrUpdate90Rule() {
        Optional<CrmOrganizationEntity> company = organizationAction.findCompanyById(1);
        Preconditions.checkState(company.isPresent());
        Optional<CrmStoreEntity> store = storeEntityAction.findById(company.get(), 8);
        Preconditions.checkState(store.isPresent());
//        careRuleEntityAction.saveOrUpdate90Rule(company.get(), Lists.newArrayList(store.get()),
//                true, false, 30, 2000, "delay=1h,expired=0d$delay=1d,expired=0d$delay=3d,expired=1d$delay=7d,expired=2d$delay=15d,expired=3d$delay=30d,expired=4d",
//                true);
    }

    @Autowired
    TaskCareRuleEntityAction careRuleEntityAction;
    @Autowired
    CrmOrganizationEntityAction organizationAction;
    @Autowired
    CrmStoreEntityAction storeEntityAction;
}