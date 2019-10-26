package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class RechargeBalanceEntityActionTest {

//    @Test
//    public void loadAllByStore() {
//        LoginContextHolder.setAnonymousCtx();
//        CrmOrganizationEntity company = companyAction.findCompanyById(1).get();
//        CrmStoreEntity store = storeAction.findById(company, 14).get();
//        Optional<List<RechargeBalanceEntity>> asd = rechargeBalanceEntityAction.loadOrderEnabledByStore(store);
//        System.out.println(asd.isPresent());
//    }

    @Autowired
    CrmOrganizationEntityAction companyAction;
    @Autowired
    CrmStoreEntityAction storeAction;
    @Autowired
    RechargeBalanceEntityAction rechargeBalanceEntityAction;
}