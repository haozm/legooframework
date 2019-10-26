package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import com.legooframework.model.membercare.entity.BusinessType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.util.Optional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class ChargeSummaryEntityActionTest {

    @Test
    public void insert() {
        LoginContextHolder.setAnonymousCtx();
        //Optional<KvDictEntity> buty = kvDictEntityAction.findByValue("SMS_BUS_TYPE", "90TOUCHED");
        SMSSendRuleEntity rule = businessRuleEntityAction.loadByType(BusinessType.TOUCHED90);
        CrmOrganizationEntity com = organizationEntityAction.findCompanyById(1).get();
        CrmStoreEntity store = storeEntityAction.findById(com, 5).get();
//        billingSummaryEntityAction.insertManual(store, rule,
//                String.format("%s_%s_201981212771212", store.getCompanyId(), store.getId()),
//                9966, "asdasdasdasdasd");
    }

    @Test
    public void findById() {
        LoginContextHolder.setAnonymousCtx();
        Optional<ChargeSummaryEntity> res = billingSummaryEntityAction.findById("3C88C03E-A43A-46BE-99F5-2F69BED2069F");
        res.ifPresent(System.out::println);
    }

    @Resource(name = "smsKvDictEntityAction")
    KvDictEntityAction kvDictEntityAction;
    @Autowired
    CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    CrmStoreEntityAction storeEntityAction;
    @Autowired
    SMSSendRuleEntityAction businessRuleEntityAction;
    @Autowired
    ChargeSummaryEntityAction billingSummaryEntityAction;
}