package com.legooframework.model.statistical.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.AsyncResult;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import com.legooframework.model.statistical.entity.DateRange;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statistical/spring-model-cfg.xml"}
)
public class StatisticalServiceTest {
    @Before
    public void info() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void loadHomePage() {
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(18, 1);
        Map<String, Object> page = statisticalService.loadHomePage(user, "EMPLOYEE_SALES");
        System.out.println(page);
    }

    @Test
    public void loadSubPage() {
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(18, 1);
        Map<String, Object> page = statisticalService.loadSubPage(user, "HOMEPAGE", "saleRecord01");
        System.out.println(page);
    }

    @Test
    public void query4Summary() {
        Map<String, Object> params = Maps.newHashMap();
        //saleRecord01.summary,saleRecord01.summary01,saleRecord01.summary02
        List<String> repids = Lists.asList("saleRecord01.summary", new String[]{"saleRecord01.summary01", "saleRecord01.summary02"});
        params.put("companyId", 1);
        List<AsyncResult> asyncResults = statisticalService.query4Summary(repids, DateRange.WEEK, new LocalDate(2019, 9, 12), null, params);
        System.out.println(asyncResults);
    }


    @Test
    public void query4SubSummary() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", 1);
        Object res = statisticalService.query4SubSummary("saleRecord04", DateRange.WEEK, new LocalDate(2019, 9, 12), null, params);
        System.out.println(((Optional) res).get());
    }

    @Test
    public void query4Detail() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", 1);
//        Object res = statisticalService.query4Detail("saleRecord04", DateRange.WEEK, new LocalDate(2019, 9, 12), null, params);
//        System.out.println(((Optional) res).get());
    }

    @Test
    public void qyery4Page() {
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(18, 1);
        Map<String, Object> layout = statisticalService.loadHomePage(user, "MEMBER");
        System.out.println("I am ok");
    }


    @Autowired
    private UserAuthorEntityAction userAuthorEntityAction;

    @Autowired
    private StatisticalService statisticalService;

}