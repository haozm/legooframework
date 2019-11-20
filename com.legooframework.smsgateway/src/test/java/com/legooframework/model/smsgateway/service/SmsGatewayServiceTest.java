package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.smsgateway.entity.SendMsg4ReimburseEntityAction;
import com.legooframework.model.smsgateway.entity.SendStatus;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import com.legooframework.model.smsresult.entity.SMSSendTransportProtocol;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsprovider/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsgateway/spring-model-cfg.xml"}
)
public class SmsGatewayServiceTest {

    @Test
    public void sendingSMSService() {
        List<String> list = Lists.newArrayList();
        for (int i = 0; i < 2; i++) {
            String payload = SMSSendTransportProtocol.encoding4Flat(UUID.randomUUID().toString(),
                    100098, 1315, SMSChannel.MarketChannel,
                    SendStatus.SMS4Sending, "18588828127", 67,
                    1, "【新的梦特娇】亲爱的肖永秀，感谢您的信任，很荣幸为您挑选到合适您的产品，请按我们沟通的洗涤方法洗涤，祝您生活愉快！");
            list.add(payload);
        }
        String payload = StringUtils.join(list, "||");
        System.out.println(payload);
        Map<String, Object> params = Maps.newHashMap();
        params.put("payload", payload);
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri("http://yg.csosm.com:6060/smsresult/api/smses/batch/sending.json?_format={format}",
                        "flat", payload)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(params)
                .retrieve().bodyToMono(String.class);
        String res = mono.block(Duration.ofSeconds(30));
        System.out.println(res);
    }

    @Test
    public void split() {
        String a = "fd42c882-855f-4605-8eda-dc1968de22d3|100098|1315|2|2|18588828127|67|1|true|%E3%80%90%E6%96%B0%E7%9A%84%E6%A2%A6%E7%89%B9%E5%A8%87%E3%80%91%E4%BA%B2%E7%88%B1%E7%9A%84%E8%82%96%E6%B0%B8%E7%A7%80%EF%BC%8C%E6%84%9F%E8%B0%A2%E6%82%A8%E7%9A%84%E4%BF%A1%E4%BB%BB%EF%BC%8C%E5%BE%88%E8%8D%A3%E5%B9%B8%E4%B8%BA%E6%82%A8%E6%8C%91%E9%80%89%E5%88%B0%E5%90%88%E9%80%82%E6%82%A8%E7%9A%84%E4%BA%A7%E5%93%81%EF%BC%8C%E8%AF%B7%E6%8C%89%E6%88%91%E4%BB%AC%E6%B2%9F%E9%80%9A%E7%9A%84%E6%B4%97%E6%B6%A4%E6%96%B9%E6%B3%95%E6%B4%97%E6%B6%A4%EF%BC%8C%E7%A5%9D%E6%82%A8%E7%94%9F%E6%B4%BB%E6%84%89%E5%BF%AB%EF%BC%81||491bab3e-e166-47ea-ab1b-586b9d4905d0|100098|1315|2|2|18588828127|67|1|true|%E3%80%90%E6%96%B0%E7%9A%84%E6%A2%A6%E7%89%B9%E5%A8%87%E3%80%91%E4%BA%B2%E7%88%B1%E7%9A%84%E8%82%96%E6%B0%B8%E7%A7%80%EF%BC%8C%E6%84%9F%E8%B0%A2%E6%82%A8%E7%9A%84%E4%BF%A1%E4%BB%BB%EF%BC%8C%E5%BE%88%E8%8D%A3%E5%B9%B8%E4%B8%BA%E6%82%A8%E6%8C%91%E9%80%89%E5%88%B0%E5%90%88%E9%80%82%E6%82%A8%E7%9A%84%E4%BA%A7%E5%93%81%EF%BC%8C%E8%AF%B7%E6%8C%89%E6%88%91%E4%BB%AC%E6%B2%9F%E9%80%9A%E7%9A%84%E6%B4%97%E6%B6%A4%E6%96%B9%E6%B3%95%E6%B4%97%E6%B6%A4%EF%BC%8C%E7%A5%9D%E6%82%A8%E7%94%9F%E6%B4%BB%E6%84%89%E5%BF%AB%EF%BC%81";
        String[] b = StringUtils.splitByWholeSeparator(a, "||");
        System.out.println(b.length);
        String result_payload = "\"4022e6e8-2fd9-4bf9-a421-4458cd78fdf3|0000|OK||9e977fa2-3f90-4d6b-8ed3-d13b4b4e5b2d|0000|OK\"";
        System.out.println(StringUtils.substring(result_payload, 1, result_payload.length() - 1));
    }

    @Test
    public void listen4SyncSMS() throws Exception {
        LoginContextHolder.setAnonymousCtx();
       // smsGatewayService.listen4SyncSMS();
    }

    @Autowired
    private SmsGatewayService smsGatewayService;
    @Autowired
    SendMsg4ReimburseEntityAction sendMsg4ReimburseEntityAction;
}