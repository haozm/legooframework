package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.BaseModelServer;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class EmployeeEntityActionTest {

    @Test
    public void findByUser() {
        OrganizationEntity com = organizationEntityAction.findCompanyById(1).get();
        Optional<EmployeeEntity> asd = action.findByLoginName("123123123", com);
        if (asd.isPresent())
            System.out.println(asd.get());
    }

    @Test
    public void findByAccount() {
    }

    @Autowired
    OrganizationEntityAction organizationEntityAction;
    @Autowired
    BaseModelServer baseModelServer;
    @Autowired
    StoreEntityAction storeEntityAction;
    @Autowired
    EmployeeEntityAction action;

    @Test
    public void addStoreEmployee() {
        Optional<StoreEntity> store = storeEntityAction.findById(6);
        LoginUserContext user = baseModelServer.loadByUserName(1, "dgqx");
//        String userName, String passowrd, String loginName, String phoneNo,
//        int sex, String remarke, Date birthday, Collection<RoleEntity> roles,
//                StoreEntity store, OrganizationEntity org, LoginUserContext loginUser
//        action.addEmployee("hao12312", "8888", "xiaojie_hao12", "12312312313", 2, "reasd", new Date(),
//                null, store.get(), null, user);
    }
}