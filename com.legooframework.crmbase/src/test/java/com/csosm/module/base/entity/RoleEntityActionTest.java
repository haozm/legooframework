package com.csosm.module.base.entity;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class RoleEntityActionTest {

    @Test
    public void loadRoleSetByUser() {
//        Optional<EmployeeEntity> employee = employeeEntityAction.findById(20);
//        Preconditions.checkState(employee.isPresent());
//        RoleSet rs = roleEntityAction.loadRoleSetByUser(employee.get());
//        System.out.println(rs);
    }

    @Autowired
    EmployeeEntityAction employeeEntityAction;
    @Autowired
    RoleEntityAction roleEntityAction;
}