package com.legooframework.model.crmadapter.entity;

import com.google.common.collect.Lists;
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
        locations = { ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-membercare-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class CrmMemberEntityActionTest {

    @Test
    public void loadByCompany() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        Optional<List<CrmMemberEntity>> aa = memberEntityAction.loadByCompany(company.get(),
                Lists.newArrayList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));
        System.out.println(aa.get());
    }

    @Autowired
    private CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    private CrmStoreEntityAction storeAction;
    @Autowired
    private CrmEmployeeEntityAction employeeAction;
    @Autowired
    private CrmMemberEntityAction memberEntityAction;
}