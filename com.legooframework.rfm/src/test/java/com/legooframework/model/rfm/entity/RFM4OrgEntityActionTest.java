package com.legooframework.model.rfm.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.legooframework.model.rfm.service.RFM4OrgService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/rfm/spring-model-cfg.xml"}
)
public class RFM4OrgEntityActionTest {
//    NamedParameterJdbcTemplat
//    JdbcTemplate

    @Test
    public void savaOrUpdateStoreRFM() throws Exception {
        LoginUserContext user = modelServer.loadByUserName(1, "dgqx098_1");
        Optional<OrganizationEntity> com = comAction.findCompanyById(1);
        Optional<StoreEntity> store = storeAction.findStoreFromCompany(7, com.get());
        action.savaOrUpdateStoreRFM(user, store.get(), 1,
                10, 30, 50, 100,
                100, 50, 30, 10,
                100, 50, 32, 10);
        Thread.sleep(2000L);
    }

    @Test
    public void batchReWriteStoreRFM() {
        LoginUserContext user = modelServer.loadByUserName(1, "dgqx098_1");
        Optional<OrganizationEntity> com = comAction.findCompanyById(1);

        Optional<List<OrganizationEntity>> lis = comAction.findOrgByIds(1, Lists.newArrayList(14));
        rfm4OrgService.savaOrUpdateCompanyRFM(user, lis.get().get(0), 1,
                20, 30, 50, 100,
                100, 50, 30, 10,
                100, 50, 30, 10, 0);
    }

    @Test
    public void savaOrUpdateCompanyRFM() {
    }

    @Test
    public void loadAllStoreRFM() {
    }

    @Test
    public void loadStoreRFM() {
    }

    @Test
    public void loadCompanyRFM() {
    }

    @Autowired
    OrganizationEntityAction comAction;
    @Autowired
    StoreEntityAction storeAction;
    @Autowired
    @Qualifier(value = "baseAdapterServer")
    BaseModelServer modelServer;
    @Autowired
    RFM4OrgEntityAction action;
    @Autowired
    RFM4OrgService rfm4OrgService;

}