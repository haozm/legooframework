package com.legooframework.model.crmadapter.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class CrmOrganizationEntityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void findCompanyById() {
        Optional<CrmOrganizationEntity> org = organizationEntityAction.findCompanyById(2);
        Assert.isTrue(org.isPresent(), "不存在的公司");
    }

    @Test
    public void loadAllByCompany() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(2);
        Assert.isTrue(company.isPresent(), "不存在的公司");
        storeEntityAction.loadAllByCompany(company.get());
    }

    @Test
    public void loadAllByStore() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(2);
        Assert.isTrue(company.isPresent(), "不存在的公司");
        storeEntityAction.loadAllByCompany(company.get());
    }

    @Test
    public void findStoreById() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(2);
        Assert.isTrue(company.isPresent(), "不存在的公司");
        Optional<CrmStoreEntity> asd = storeEntityAction.findById(company.get(), 7204);
        org.junit.Assert.assertTrue(asd.isPresent());
    }

    @Test
    public void loadEmpAllByStore() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(2);
        Optional<CrmStoreEntity> storeEntity = storeEntityAction.findById(company.get(), 2922);
        Optional<List<CrmEmployeeEntity>> asd = employeeEntityAction.loadAllByStore(storeEntity.get());
        System.out.println(asd.get().toString());
    }

    @Test
    public void loadMmAllByStore() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(2);
        Optional<CrmStoreEntity> storeEntity = storeEntityAction.findById(company.get(), 2922);
        Optional<List<CrmMemberEntity>> asd = memberEntityAction.loadAllByStore(storeEntity.get());
        System.out.println(asd.get().size());
    }

    @Autowired
    private CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    private CrmStoreEntityAction storeEntityAction;
    @Autowired
    private CrmEmployeeEntityAction employeeEntityAction;
    @Autowired
    private CrmMemberEntityAction memberEntityAction;
}