package com.legooframework.model.regiscenter.entity;

;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/organization/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/regiscenter/spring-model-cfg.xml"}
)
public class TenantNetConfigEntityActionTest {

    @Test
    public void findByCompany() {
//        LoginContextHolder.setCtx(new LoginContextTest());
//        Optional<CompanyEntity> com = companyEntityAction.findById(100000000L);
//        Assert.assertTrue(com.isPresent());
//        Optional<TenantNetConfigEntity> ty = configEntityAction.findByCompany(com.get());
//        Assert.assertTrue(ty.isPresent());
    }

//    @Autowired
//    CompanyEntityAction companyEntityAction;
    @Autowired
    TenantNetConfigEntityAction configEntityAction;
}