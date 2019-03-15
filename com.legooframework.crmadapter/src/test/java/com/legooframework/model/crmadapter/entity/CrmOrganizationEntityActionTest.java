package com.legooframework.model.crmadapter.entity;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
public class CrmOrganizationEntityActionTest {

    @Before
    public void init() {
//        UsernamePasswordAuthenticationFilter
    }

    @Test
    public void findCompanyById() {
        Optional<CrmOrganizationEntity> org = organizationEntityAction.findCompanyById(1);
    }

    @Test
    public void loadOrganizations() {
        organizationEntityAction.loadOrganizations(1);
    }

    //    @Test
//    public void loadAllByCompany() {
//        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(2);
//        Assert.isTrue(company.isPresent(), "不存在的公司");
//        storeEntityAction.loadAllByCompany(company.get());
//    }
//


    //
//    @Test
//    public void findStoreById() {
//        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(2);
//        Assert.isTrue(company.isPresent(), "不存在的公司");
//        Optional<CrmStoreEntity> asd = storeEntityAction.findById(company.get(), 7204);
//        org.junit.Assert.assertTrue(asd.isPresent());
//    }
//
    @Test
    public void loadEmpAllByStore() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        Optional<CrmStoreEntity> storeEntity = storeEntityAction.findById(company.get(), 15);
        Optional<List<CrmEmployeeEntity>> asd = employeeEntityAction.loadAllByStore(storeEntity.get());
        System.out.println(asd.get().toString());
    }

    @Test
    public void loadEmpById() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        Optional<CrmEmployeeEntity> asd = employeeEntityAction.findById(company.get(), 1);
        System.out.println(asd.get().toString());
    }

    @Test
    public void loadEmpByName() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        Optional<CrmEmployeeEntity> asd = employeeEntityAction.findByLoginName(company.get(), "dgqx");
        System.out.println(asd.get().toString());
    }

    @Test
    public void loadMmAllByStore() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        Optional<CrmStoreEntity> storeEntity = storeEntityAction.findById(company.get(), 11);
        Optional<List<CrmMemberEntity>> asd = memberEntityAction.loadAllByStore(storeEntity.get());
        System.out.println(asd.get().size());
    }

    @Test
    public void loadMmAllByIds() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        // Optional<CrmStoreEntity> storeEntity = storeEntityAction.findById(company.get(), 11);
        Optional<List<CrmMemberEntity>> asd = memberEntityAction.loadByCompany(company.get(),
                Sets.newHashSet(12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26));
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