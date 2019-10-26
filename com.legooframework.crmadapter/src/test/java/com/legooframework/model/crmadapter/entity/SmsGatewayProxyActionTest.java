package com.legooframework.model.crmadapter.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.membercare.entity.BusinessType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml"}
)
public class SmsGatewayProxyActionTest {

    @Test
    public void sendMessageProxy() {
        String m01 = "-1,1126,2";
        // String m02 = "-1,1127,2";
        List<String> payloads = Lists.newArrayList(m01);
        smsGatewayProxyAction.sendMessageProxy(100098, 1316, 33, payloads, "你妈妈喊你汇集哎吃饭了",
                BusinessType.BATCHCARE, true, null);
    }

    @Autowired
    SmsGatewayProxyAction smsGatewayProxyAction;
}