package com.legooframework.model.membercare.entity;

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

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-membercare-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/membercare/spring-model-cfg.xml"}
)
public class Touch90TemplateEntityActionTest {

    private String rule = "delay=1h,expired=0d$delay=1d,expired=0d$delay=3d,expired=1d$delay=7d," +
            "expired=1d$delay=15d,expired=1d$delay=30d,expired=1d$delay=60d,expired=1d$delay=90d,expired=1d";
    private String rule1 = "delay=1d,expired=0d$delay=3d,expired=1d$delay=7d,expired=1d";
    private String autoRunBuilderSpec = "channel=1,singleSalesAmount=3000";

    String meg = "maxConsumptionDays=30,maxAmountOfconsumption=1000,cancelBefore=true";

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void loadByCompany() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        touch90TemplateEntityAction.loadByCompany(company.get());
    }

    @Test
    public void saveOrUpdate() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        List<CrmStoreEntity> stores = storeEntityAction.loadAllByCompany(company.get());
        TaskCareRule4Touch90Entity entity = TaskCareRule4Touch90Entity.createRuleTemplate("2", meg, rule);
        touch90TemplateEntityAction.saveOrUpdate(entity, true, stores, LoginContextHolder.get());
    }

    @Autowired
    CrmStoreEntityAction storeEntityAction;
    @Autowired
    CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    private Touch90TemplateEntityAction touch90TemplateEntityAction;
}