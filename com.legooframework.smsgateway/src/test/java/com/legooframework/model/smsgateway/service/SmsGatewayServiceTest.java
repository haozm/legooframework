package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.AutoRunChannel;
import com.legooframework.model.smsgateway.entity.SendMessageBuilder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
//            String payload = SMSSendTransportProtocol.encoding4Flat(UUID.randomUUID().toString(),
//                    100098, 1315, SMSChannel.MarketChannel,
//                    SendStatus.SMS4Sending, "18588828127", 67,
//                    1, "【新的梦特娇】亲爱的肖永秀，感谢您的信任，很荣幸为您挑选到合适您的产品，请按我们沟通的洗涤方法洗涤，祝您生活愉快！");
            list.add(null);
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

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayServiceTest.class);

    @Test
    public void batchSaveMessage() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        String sql = "SELECT cm.id from acp.crm_member cm \n" +
                "inner JOIN acp.crm_store_member csm on csm.member_id =cm.id\n" +
                "where csm.store_id=15 and cm.status=1 limit 3000";
        List<Integer> cids = jdbcQuerySupport.getJdbcTemplate().queryForList(sql, Integer.class);
        logger.debug(String.format("jdbcQuerySupport() mmdis is %d", cids.size()));
        StoEntity store = stoEntityAction.loadById(15);
        UserAuthorEntity user = userAuthorEntityAction.loadUserById(15, 1);
        List<SendMessageBuilder> msgList = cids.stream().map(x -> SendMessageBuilder.createWithoutJobNoTemplate(BusinessType.BIRTHDAYTOUCH,
                x, AutoRunChannel.SMS_ONLY)).collect(Collectors.toList());
//        smsGatewayService.batchSaveMessage(store, msgList,
//                "【新的梦特娇】亲爱的{会员姓名}，感谢您的信任，很荣幸为您挑选到合适您的产品，请按我们沟通的洗涤方法洗涤，祝您生活愉快！", user);
    }


    @Resource(name = "smsJdbcQuerySupport")
    private JdbcQuerySupport jdbcQuerySupport;
    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private SmsGatewayService smsGatewayService;
    @Autowired
    private UserAuthorEntityAction userAuthorEntityAction;
}