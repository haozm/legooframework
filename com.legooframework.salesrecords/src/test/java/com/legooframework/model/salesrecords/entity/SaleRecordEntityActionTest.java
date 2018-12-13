package com.legooframework.model.salesrecords.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.*;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
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
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml"}
)
public class SaleRecordEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void findById() {
        Optional<SaleRecordEntity> as = saleRecordEntityAction.findById(685160);
        Assert.assertTrue(as.isPresent());
    }

    @Test
    public void findSampleById() {
        Optional<SaleRecordEntity> as = saleRecordEntityAction.findSampleById(685160);
        Assert.assertTrue(as.isPresent());
    }

    @Test
    public void loadByStore() {
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(1);
        Assert.assertTrue(company.isPresent());
        Optional<List<CrmStoreEntity>> stores = storeEntityAction.loadAllByCompany(company.get());
        Assert.assertTrue(stores.isPresent());
        LocalDateTime startDay = DateTimeUtils.parseDef("2018-11-20 00:00:00");
        LocalDateTime endDay = DateTimeUtils.parseDef("2018-12-01 23:59:59");
        Optional<List<SaleRecordEntity>> entities = saleRecordEntityAction.loadByDateInterval(stores.get(), startDay, endDay, true);
        entities.ifPresent(x -> System.out.println(x.size()));
    }

    @Autowired
    CrmMemberEntityAction memberEntityAction;
    @Autowired
    private CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    private CrmStoreEntityAction storeEntityAction;
    @Autowired
    private SaleRecordEntityAction saleRecordEntityAction;

}