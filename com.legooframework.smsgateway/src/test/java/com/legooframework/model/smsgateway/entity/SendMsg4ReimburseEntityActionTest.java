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

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-smsclient-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SendMsg4ReimburseEntityActionTest {

    @Test
    public void loadBySendBatchNo() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<SendMsg4ReimburseEntity>> res = sendMsg4ReimburseEntityAction.loadBySendBatchNo("100098_1315_931626dLLUZB");
        res.ifPresent(System.out::println);
    }

    @Test
    public void batchReimburse() {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<SendMsg4ReimburseEntity>> res = sendMsg4ReimburseEntityAction.loadBySendBatchNo("100098_1315_931626dLLUZB");
        res.ifPresent(System.out::println);
        sendMsg4ReimburseEntityAction.batchReimburse(res.get());
    }

    @Autowired
    SendMsg4ReimburseEntityAction sendMsg4ReimburseEntityAction;
}