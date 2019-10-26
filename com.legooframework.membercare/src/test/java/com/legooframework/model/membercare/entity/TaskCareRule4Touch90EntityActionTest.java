package com.legooframework.model.membercare.entity;

import com.google.common.collect.Lists;
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
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-membercare-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class TaskCareRule4Touch90EntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    private String rule = "delay=1h,expired=0d$delay=1d,expired=0d$delay=3d,expired=1d$delay=7d," +
            "expired=1d$delay=15d,expired=1d$delay=30d,expired=1d$delay=60d,expired=1d$delay=90d,expired=1d";
    private String rule1 = "delay=1d,expired=0d$delay=3d,expired=1d$delay=7d,expired=1d";
    private String autoRunBuilderSpec = "channel=1,singleSalesAmount=3000";

    String meg = "maxConsumptionDays=30,maxAmountOfconsumption=1000,cancelBefore=true";

    @Test
    public void saveOrUpdateTouch90RuleTemplate() {
        taskCareRule4Touch90EntityAction.saveOrUpdateTouch90RuleTemplate("1", meg, rule);
        taskCareRule4Touch90EntityAction.saveOrUpdateTouch90RuleTemplate(null, meg, rule);
        taskCareRule4Touch90EntityAction.saveOrUpdateTouch90RuleTemplate("2", meg, rule);
        taskCareRule4Touch90EntityAction.saveOrUpdateTouch90RuleTemplate("3", meg, rule);
        taskCareRule4Touch90EntityAction.saveOrUpdateTouch90RuleTemplate("4", meg, rule);
    }

    @Test
    public void addTouch90RuleToCompany01() {
        TaskCareRule4Touch90Entity entity = TaskCareRule4Touch90Entity.createCompanyRule("2", null, rule, autoRunBuilderSpec,
                true, LoginContextHolder.get());
        taskCareRule4Touch90EntityAction.addTouch90Rule(entity, true, null, LoginContextHolder.get());
//        TaskCareRule4Touch90Entity entity1 = TaskCareRule4Touch90Entity.createRuleTemplate("1", meg, rule);
//        taskCareRule4Touch90EntityAction.addTouch90Rule(entity1, true, null, LoginContextHolder.get());
    }

    @Test
    public void addTouch90RuleToCompany() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        List<CrmStoreEntity> stores = storeEntityAction.loadAllByCompany(company.get());
        TaskCareRule4Touch90Entity entity = TaskCareRule4Touch90Entity.createRuleTemplate("2", meg, rule1);
        taskCareRule4Touch90EntityAction.addTouch90Rule(entity, true, null, LoginContextHolder.get());
        // taskCareRule4Touch90EntityAction.addTouch90Rule("3", stores, LoginContextHolder.get());
//        taskCareRule4Touch90EntityAction.addTouch90Rule("1", null, LoginContextHolder.get());
//        taskCareRule4Touch90EntityAction.addTouch90Rule(null, null, LoginContextHolder.get());
    }

    @Test
    public void removeTouch90RuleToCompany() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        List<CrmStoreEntity> stores = storeEntityAction.loadAllByCompany(company.get());
        taskCareRule4Touch90EntityAction.removeTouch90Rule("2", false, stores, LoginContextHolder.get());
        // taskCareRule4Touch90EntityAction.addTouch90Rule("3", stores, LoginContextHolder.get());
//        taskCareRule4Touch90EntityAction.addTouch90Rule("1", null, LoginContextHolder.get());
//        taskCareRule4Touch90EntityAction.addTouch90Rule(null, null, LoginContextHolder.get());
    }

    @Test
    public void updateRuleToCompanyOrStores() {
        String rule_test = "delay=1h,expired=0d$delay=1d,expired=0d$delay=3d,expired=1d$delay=7d,expired=1d$delay=13d,expired=1d";
        TaskCareRule4Touch90Entity entity_01 = TaskCareRule4Touch90Entity.createRuleTemplate("2", meg, rule_test);
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        List<CrmStoreEntity> stores = storeEntityAction.loadAllByCompany(company.get());
        taskCareRule4Touch90EntityAction.updateTouch90Rule(true, null, entity_01, LoginContextHolder.get());
    }

    @Test
    public void loadAll90Categories() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        Optional<Set<String>> sets = taskCareRule4Touch90EntityAction.loadAll90Categories(company.get());
        sets.ifPresent(System.out::println);
    }


    @Test
    public void disabledTouch90Rule() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        List<CrmStoreEntity> stores = storeEntityAction.loadAllByCompany(company.get());
        taskCareRule4Touch90EntityAction.disabledTouch90Rule("1", false, stores, LoginContextHolder.get());
    }

    @Test
    public void enabledTouch90Rule() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        List<CrmStoreEntity> stores = storeEntityAction.loadAllByCompany(company.get());
        taskCareRule4Touch90EntityAction.disabledTouch90Rule("2", false, stores, LoginContextHolder.get());
    }

    @Test
    public void loadEnabledTouch90RuleTemplates() {
        Optional<List<TaskCareRule4Touch90Entity>> list = taskCareRule4Touch90EntityAction.loadEnabledTouch90RuleTemplates();
        list.ifPresent(x -> System.out.println(x.size()));
    }

    @Autowired
    CrmStoreEntityAction storeEntityAction;
    @Autowired
    CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    TaskCareRule4Touch90EntityAction taskCareRule4Touch90EntityAction;
}