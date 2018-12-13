package com.legooframework.model.salesrecords.service;

import com.google.common.collect.Sets;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.salesrecords.dto.SaleRecordByMember;
import com.legooframework.model.salesrecords.entity.LoginContextTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Collections;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml"}
)
public class SaleRecordServiceTest {
    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setCtx(new LoginContextTest());
    }

    @Test
    public void loadMergeSaleRecords() {
        String start = "2018-03-15 00:00:00";
        String end = "2018-03-31 23:59:59";
        Integer[] ids = new Integer[]{3493, 2952, 3541, 3126, 3145, 2923, 4349, 5283, 5202, 3344, 2933, 6568, 3276, 4826};
        Set<Integer> asd = Sets.newHashSet();
        Collections.addAll(asd, ids);
//        SaleRecordByMember res = saleRecordService.loadMergeSaleRecords(2, asd, start, end, true);
//        System.out.println(res);
    }

    @Autowired
    SaleRecordService saleRecordService;
}