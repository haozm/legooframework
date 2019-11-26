package com.legooframework.model.covariant.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class CovariantServiceTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void loadMemberIds() {
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(15, 1);
        Map<String, String> params = Maps.newHashMap();
        params.put("rfm.minReccencyLevel", "5");
        params.put("rfm.maxReccencyLevel", "5");
        params.put("rfm.minFrenquencyLevel", "5");
        params.put("rfm.maxFrenquencyLevel", "5");
        params.put("rfm.minMonetaryLevel", "5");
        params.put("rfm.maxMonetaryLevel", "5");
        params.put("beginDate", "2019-11-25");
        params.put("endDate", "2019-11-26");
        params.put("other", "quicksearch:1;birthday:2019-10-11,2019-12-12;consumeTotalAmount:1,100,1000;consumeTotalTimes:1,1,8;cusUnitPrice:100,500;customDate:2019-11-16,2019-11-26;notCustomDate:2019-11-06,2019-11-26;addMemberDate:2019-11-01,2019-11-26;totalScore:1,1000;labelIds:200103");
        covariantService.loadMemberIds(2, params, user);
    }


    @Test
    public void preSendSmsByStore() {
        covariantService.preSendSmsByStore(1120, 389255, Lists.newArrayList(4249, 4250, 4251, 4252),
                BusinessType.BIRTHDAYTOUCH, "{会员姓名}你好");
    }

    @Test
    public void loadStoreAgg() {
        covariantService.loadStoreAgg(1120);
        System.out.println("------------------------");
        covariantService.loadStoreAgg(1120);
    }


    @Autowired
    UserAuthorEntityAction userAuthorEntityAction;
    @Autowired
    CovariantService covariantService;
}