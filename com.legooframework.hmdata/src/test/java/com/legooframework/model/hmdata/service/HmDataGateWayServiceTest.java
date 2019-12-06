package com.legooframework.model.hmdata.service;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/hmdata/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/hmdata/spring-test-cfg.xml"}
)
public class HmDataGateWayServiceTest {

    @Test
    public void bankCardSign() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("version", "1.0");
        param.put("merchant_no", "1018");
        param.put("sign_order_no", "AP2I1110182122312212212255");
        param.put("amount", "20000");
        param.put("payer_name", "xxx");
        param.put("payer_id_card", "xxxxxxxxxxx");
        param.put("payer_bank_card_no", "xxxxxxxxxxxxx");
        param.put("bank_mobile", "xxxxxxxxxxx");
        param.put("bank_code", "900000001");
        param.put("tied_card_type", "1000");
        gateWayService.bankCardSign(param);
    }

    @Test
    public void bankCardVerify() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("version", "1.0");
        param.put("merchant_no", "1018");
        param.put("sign_order_no", "AP2I1110182122312212212255");
        param.put("sms_code", "429314");
        gateWayService.bankCardVerify(param);
    }

    @Test
    public void quickPayment() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("version", "1.0");
        param.put("merchant_no", "1018");
        param.put("sign_order_no", "AP2I1110182122312212212255");
        param.put("pay_order_no", "PAY123123123123123123123");
        param.put("pay_amount", "5000");
        param.put("submit_time", "2019-08-13 16:58:09");
        param.put("notify_url", "https://hmtest.hmdata.com.cn/tool/backfun/API");
        gateWayService.quickPayment(param);
    }

    @Test
    public void payQueryOrder() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("version", "1.0");
        param.put("merchant_no", "1018");
        param.put("pay_order_no", "PAY1566371037637");
        gateWayService.payQueryOrder(param);
    }

    @Test
    public void bankCardRelieve() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("version", "1.0");
        param.put("merchant_no", "1018");
        param.put("user_bind_id", "AIP2683190604001780683");
        gateWayService.bankCardRelieve(param);
    }

    @Test
    public void drawApply() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("version", "1.0");
        param.put("merchant_no", "1018");
        param.put("draw_money", "2000");
        gateWayService.drawApply(param);
    }


    @Autowired
    private HmDataGateWayService gateWayService;
}