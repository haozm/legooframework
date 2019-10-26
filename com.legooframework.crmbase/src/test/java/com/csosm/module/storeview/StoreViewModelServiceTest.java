package com.csosm.module.storeview;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.*;
import com.csosm.module.storeview.entity.StoreTreeViewDto;
import com.csosm.module.storeview.entity.StoreViewEntityAction;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/jwtoken/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class StoreViewModelServiceTest {

    @Test
    public void loadSmsRechargeTree() {
        LoginUserContext user = baseAction.loadByUserName(1, "dgqx");
        OrganizationEntity com = orgAction.findCompanyById(1).get();
        storeViewModelService.loadSmsRechargeTree(com, user);
        //   Optional<EmployeeEntity> employee = empAction.findById(20);
//        StoreTreeViewDto st = storeViewModelService.loadStoreViewTreeByUser(employee.get(), user);
//        System.out.println(st);
    }

    @Autowired
    private StoreEntityAction strAction;
    @Autowired
    private OrganizationEntityAction orgAction;
    @Autowired
    private EmployeeEntityAction empAction;
    @Autowired
    private BaseModelServer baseAction;
    @Autowired
    private StoreViewEntityAction viewAction;
    @Autowired
    StoreViewService storeViewModelService;
}