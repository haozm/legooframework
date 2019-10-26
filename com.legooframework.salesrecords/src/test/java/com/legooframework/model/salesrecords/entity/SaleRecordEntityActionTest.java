package com.legooframework.model.salesrecords.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.MemberEntityAction;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-sale-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml"}
)
public class SaleRecordEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void loadMemberBy90Days() {
//        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
//        Assert.assertTrue(company.isPresent());
//        Optional<CrmStoreEntity> stores = storeEntityAction.findById(company.get(), 1314);
//        System.out.println(stores.isPresent());
//        Optional<CrmMemberEntity> mms = memberEntityAction.loadMemberByCompany(company.get(), 5);
//        saleRecordEntityAction.loadMemberBy90Days(mms.get(), stores.get());
    }

    @Test
    public void loadByStore() {
//        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
//        Assert.assertTrue(company.isPresent());
//        List<CrmStoreEntity> stores = storeEntityAction.loadAllByCompany(company.get());
//        LocalDateTime startDay = DateTimeUtils.parseDef("2018-11-20 00:00:00");
//        LocalDateTime endDay = DateTimeUtils.parseDef("2019-12-01 23:59:59");
//        Optional<List<SaleRecordEntity>> entities = saleRecordEntityAction.loadByDateInterval(stores.get(0), "0", startDay, endDay, false);
//        entities.ifPresent(x -> System.out.println(x.size()));
    }

    @Autowired
    MemberEntityAction memberEntityAction;
    @Autowired
    private OrgEntityAction organizationEntityAction;
    @Autowired
    private StoEntityAction storeEntityAction;
    @Autowired
    private SaleRecordEntityAction saleRecordEntityAction;

}