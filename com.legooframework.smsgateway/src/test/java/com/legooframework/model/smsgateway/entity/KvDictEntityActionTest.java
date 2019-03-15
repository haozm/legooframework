package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class KvDictEntityActionTest {

    @Test
    public void test() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<KvDictEntity>> list = dictEntityAction.findByType("SMS_BUS_TYPE");
        System.out.println(list.isPresent());
    }

    @Resource(name = "smsKvDictEntityAction")
    KvDictEntityAction dictEntityAction;
}
