package com.legooframework.model.dict.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/integration/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/dict/spring-model-cfg.xml"}
)
public class KvDictEntityActionTest {

    @Test
    public void findById() {
    }

    @Test
    public void findByValue() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<KvDictEntity> entity = dictEntityAction.findByValue("SEX", "-1");
        entity.ifPresent(System.out::println);
    }

    @Test
    public void findByType() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<List<KvDictEntity>> entity = dictEntityAction.findByType("SEX");
        entity.ifPresent(x -> System.out.println(x.size()));
        entity.ifPresent(x -> x.forEach(System.out::println));
    }

    //insert

    @Test
    public void insert() {
        LoginContextHolder.setCtx(new LoginContextTest());
        dictEntityAction.insert("SEX", "10", "测试11", "秒睡", 9);
    }


    // deleteByEntity
    @Test
    public void deleteByEntity() {
        LoginContextHolder.setCtx(new LoginContextTest());
        Optional<KvDictEntity> entity = dictEntityAction.findByValue("SEX", "9");
        if (entity.isPresent()) {
            int res = dictEntityAction.deleteByEntity(entity.get());
            System.out.println(res);
        }
    }

    @Autowired
    private KvDictEntityAction dictEntityAction;
}