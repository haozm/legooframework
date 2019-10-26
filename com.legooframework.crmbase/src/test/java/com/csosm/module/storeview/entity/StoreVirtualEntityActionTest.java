package com.csosm.module.storeview.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.*;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/jwtoken/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class StoreVirtualEntityActionTest {

    @Test
    public void addRootNode() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx085_1");
        // Optional<EmployeeEntity> employee = empAction.findById(27);
        // viewAction.addDataPermissionTreeRootNode(employee.get(), user);
    }

    @Test
    public void addSubGroupNode() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx");
        viewAction.addSubGroupNode("3cc2e210-d1d5-4760-b4b2-fead35ef551f", "四上窜汇总", "hubeida", user);
    }

    @Test
    public void editGroupNodeName() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx");
        viewAction.editGroupNodeName("3cc2e210-d1d5-4760-b4b2-fead35ef551f", "四川大区", "四川", user);
    }

    @Test
    public void removeSubNodeById() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx");
        viewAction.removeSubNodeById("1c90b87d-5411-4391-b51f-c9253b666d22", user);
    }

    @Test
    public void addStoresToNode() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx");
        List<StoreEntity> strs = strAction.findByIds(Lists.newArrayList(26,27)).get();
        viewAction.addStoresToNode("642e2689-7962-4ddc-af05-eb00fb4dd69a", strs, user);
    }


    @Test
    public void loadSmsRechargeTree() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx");
        Optional<OrganizationEntity> orfg = orgAction.findCompanyById(1);
        //   List<StoreEntity> strs = strAction.findByIds(Lists.newArrayList(21, 22)).get();
        viewAction.loadSmsRechargeTree(orfg.get(), user);
    }

    @Autowired
    private OrganizationEntityAction orgAction;
    @Autowired
    private StoreEntityAction strAction;
    @Autowired
    private EmployeeEntityAction empAction;
    @Autowired
    private BaseModelServer baseAction;
    @Autowired
    private StoreViewEntityAction viewAction;
}