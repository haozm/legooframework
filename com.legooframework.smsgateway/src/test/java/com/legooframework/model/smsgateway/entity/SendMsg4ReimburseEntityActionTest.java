package com.legooframework.model.smsgateway.entity;

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
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsprovider/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/entities/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SendMsg4ReimburseEntityActionTest {

    @Test
    public void loadBySendBatchNo() {
        LoginContextHolder.setAnonymousCtx();
//        Optional<List<SendMsg4ReimburseEntity>> res = sendMsg4ReimburseEntityAction.loadBySendBatchNo("100098_1315_931626dLLUZB");
//        res.ifPresent(System.out::println);
    }

    @Test
    public void batchReimburse() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<ReimburseResDto>> res = sendMsg4ReimburseEntityAction.loadUnReimburseDto();
        res.ifPresent(System.out::println);
        for (ReimburseResDto $it : res.get()) {
            sendMsg4ReimburseEntityAction.updateReimburseState($it);
        }
    }

    @Autowired
    SendMsg4ReimburseEntityAction sendMsg4ReimburseEntityAction;
}