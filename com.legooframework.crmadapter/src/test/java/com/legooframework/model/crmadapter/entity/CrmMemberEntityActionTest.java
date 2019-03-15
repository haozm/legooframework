package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class CrmMemberEntityActionTest {

    @Test
    public void loadAllByStore() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        Optional<CrmStoreEntity> ss = storeAction.findById(company.get(), 10);
        Optional<List<CrmEmployeeEntity>> aa = employeeAction.loadAllByStore(ss.get());
        System.out.println(aa.get());
    }

    @Autowired
    private CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    private CrmStoreEntityAction storeAction;
    @Autowired
    private CrmEmployeeEntityAction employeeAction;
}