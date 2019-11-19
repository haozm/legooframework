package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsprovider/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/entities/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class RechargeBalanceEntityActionTest {

    @Test
    public void createStoreGroupBalance() {
        LoginContextHolder.setAnonymousCtx();
        OrgEntity company = companyAction.loadComById(1);
        Optional<List<StoEntity>> stores = storeAction.findByIds(10, 11, 12, 14, 15);
        rechargeBalanceEntityAction.createStoreGroupBalance(company, stores.get(), "第一分组");
    }

    @Autowired
    OrgEntityAction companyAction;
    @Autowired
    StoEntityAction storeAction;
    @Autowired
    RechargeBalanceEntityAction rechargeBalanceEntityAction;
}