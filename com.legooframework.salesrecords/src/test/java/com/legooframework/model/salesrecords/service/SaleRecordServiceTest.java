package com.legooframework.model.salesrecords.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.salesrecords.entity.LoginContextTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml"}
)
public class SaleRecordServiceTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void loadMergeSaleRecords() {
        LocalDateTime start = DateTimeUtils.parseDef("2018-09-15 00:00:00");
        LocalDateTime end = DateTimeUtils.parseDef("2018-12-14 23:59:59");
        //      saleRecordService.loadSaleRecordByStore(67, null, start.toDate(), end.toDate(), true);
//        System.out.println(res);
    }

    @Autowired
    SaleRecordService saleRecordService;

    @Test
    public void saleRecord4EmployeeJob() {
        saleRecordService.alloctSaleOrder4EmployeeJob();
    }
}