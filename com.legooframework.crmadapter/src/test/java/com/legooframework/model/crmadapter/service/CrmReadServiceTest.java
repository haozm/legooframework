package com.legooframework.model.crmadapter.service;

import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Collection;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class CrmReadServiceTest {
    @Test
    public void loadByLoginName() {
        crmReadService.loadByLoginName(1, "dgqx");
    }
//    SecurityContextPersistenceFilter
//    ExceptionTranslationFilter
//    LoginUrlAuthenticationEntryPoint
    //    @Test
//    public void loadSubStores() {
//        Optional<Collection<CrmStoreEntity>> strs = crmReadService.loadSubStores(1, 1);
//        strs.ifPresent(System.out::println);
//    }
//
    @Autowired
    private CrmReadService crmReadService;
}