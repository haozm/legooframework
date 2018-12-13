package com.legooframework.model.organization.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.organization.LoginContextTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/base/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/organization/spring-model-cfg.xml"}
)
public class EmployeeEntityActionTest {

    @Test
    public void insert() {
        LoginContextHolder.setCtx(new LoginContextTest());
        for (int i = 2; i < 10; i++) {
//            employeeEntityAction.insert("店长00" + i, null, new Date(),
//                    "测试店长-" + i, "0000000" + i, "1858882811" + i);
        }

    }

    @Test
    public void findById() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<EmployeeEntity> opt = employeeEntityAction.findById("A52001C69F01A8");
        opt.ifPresent(System.out::println);
    }

    @Autowired
    private EmployeeEntityAction employeeEntityAction;

}