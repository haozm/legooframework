package com.legooframework.model.takecare.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/takecare/spring-model-cfg.xml"}
)
public class CareNinetyRuleEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void saveByCompany() {
//        OrgEntity com = orgEntityAction.loadComById(1);
//        careNinetyRuleEntityAction.saveByCompany(com, 0, 1, 2, 3, 4, 5, 6, 0, "公司", 30, 12.00D, 35.00D, true);
    }

    @Test
    public void saveByStore() {
//        StoEntity store = stoEntityAction.loadById(1120);
//        careNinetyRuleEntityAction.saveByStore(store, 0, 1, 2, 3, 4, 5, 6, 0, "门店1120", 30, 12.00D, 35.00D);
    }


    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private OrgEntityAction orgEntityAction;
    @Autowired
    private CareNinetyRuleEntityAction careNinetyRuleEntityAction;
}