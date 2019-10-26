package com.legooframework.model.covariant.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class OrgEntityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void loadOrgByStore() {
        Optional<StoEntity> sto = stoEntityAction.findById(2);
        System.out.println(orgEntityAction.loadOrgByStore(sto.get()));
    }

    @Test
    public void loadAllOrgsByCompanyId() {
        orgEntityAction.loadAllOrgsByCompanyId(1);
    }

    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private OrgEntityAction orgEntityAction;


}