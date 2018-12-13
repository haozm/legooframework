package com.legooframework.model.organization.service;

import com.legooframework.model.commons.dto.TreeStructure;
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
public class StoreServiceTest {

    @Test
    public void loadAllStoreTree() {
        LoginContextHolder.setCtx(new LoginContextTest());
        TreeStructure tree = storeService.loadAllStoreTree(100000000L);
        System.out.println(tree);
        //tree.getAllChildren().ifPresent(x -> System.out.println(x.size()));
    }

    @Test
    public void getChildById() {
        LoginContextHolder.setCtx(new LoginContextTest());
        TreeStructure tree = storeService.loadAllStoreTree(100000000L);
        Optional<TreeStructure> child = tree.getChildById(String.format("ORG_%s", 1000004L));
        child.ifPresent(System.out::println);
    }

    @Test
    public void loadOpeningStoresByCompany() {
    }

    @Test
    public void bindingDeviceToStore() {
        LoginContextHolder.setCtx(new LoginContextTest());
        storeService.bindingDeviceToStore(100000010L, "846742030134656");
    }


    @Autowired
    StoreService storeService;
}