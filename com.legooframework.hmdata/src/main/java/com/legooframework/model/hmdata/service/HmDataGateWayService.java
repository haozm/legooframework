package com.legooframework.model.hmdata.service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.hmdata.entity.GsonUtil;
import com.legooframework.model.hmdata.entity.HmDataApiConfig;
import com.legooframework.model.hmdata.entity.HttpPostAction;
import com.legooframework.model.hmdata.entity.RSAUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class HmDataGateWayService {

    private static final Logger logger = LoggerFactory.getLogger(HmDataGateWayService.class);

    public HmDataGateWayService() {
    }

    /**
     * 短信绑卡鉴权接口
     */
    public Map<String, String> bankCardSign(Map<String, Object> requestMap) {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> params = Maps.newHashMap(requestMap);
        String sign = RSAUtils.signByPrivateKey(RSAUtils.sortString(GsonUtil.toJson(params)), hmDataApiConfig.getPrivateKey());
        params.put("sign", sign);
        if (logger.isInfoEnabled())
            logger.info(String.format("[---------------- bankCardSign-request (%s)--------------------]\n%s", uuid, params));
        String response_str = this.httpPostAction.post(this.hmDataApiConfig.getCardBindSign(), params, uuid);
        Map<String, String> responseMap = GsonUtil.fromJsonByMap(response_str);
        verifySign(responseMap);
        return responseMap;
    }

    private void verifySign(Map<String, String> response) {
        Map<String, String> params = Maps.newHashMap(response);
        if (StringUtils.equals("000000", MapUtils.getString(params, "resp_code"))) {
            String sign = params.remove("sign");
            if (Strings.isNullOrEmpty(sign)) {
                response.put("sign_code", "9999");
                response.put("sign_msg", "该报文缺失签名...");
                return;
            }
            boolean verifyFlg = RSAUtils.verifyByPublicKey(sign, RSAUtils.sortString(GsonUtil.toJson(params)),
                    hmDataApiConfig.getPublicKey());
            if (!verifyFlg) {
                response.put("sign_code", "9998");
                response.put("sign_msg", "签名验证失败...");
            }
        }
    }

    /**
     * 短信绑卡鉴权接口
     */
    public Map<String, String> bankCardVerify(Map<String, Object> requestMap) {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> params = Maps.newHashMap(requestMap);
        String sign = RSAUtils.signByPrivateKey(RSAUtils.sortString(GsonUtil.toJson(params)), hmDataApiConfig.getPrivateKey());
        params.put("sign", sign);
        if (logger.isInfoEnabled())
            logger.info(String.format("[---------------- bankCardVerify-request (%s)--------------------]\n%s", uuid, params));
        String response_str = this.httpPostAction.post(this.hmDataApiConfig.getCardBindVerify(), params, uuid);
        Map<String, String> responseMap = GsonUtil.fromJsonByMap(response_str);
        verifySign(responseMap);
        return responseMap;
    }

    /**
     * 快捷支付接口
     */
    public Map<String, String> quickPayment(Map<String, Object> requestMap) {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> params = Maps.newHashMap(requestMap);
        String sign = RSAUtils.signByPrivateKey(RSAUtils.sortString(GsonUtil.toJson(params)), hmDataApiConfig.getPrivateKey());
        params.put("sign", sign);
        if (logger.isInfoEnabled())
            logger.info(String.format("[---------------- quickPayment-request (%s)--------------------]\n%s", uuid, params));
        String response_str = this.httpPostAction.post(this.hmDataApiConfig.getQuickPayment(), params, uuid);
        Map<String, String> responseMap = GsonUtil.fromJsonByMap(response_str);
        verifySign(responseMap);
        return responseMap;

    }

    /**
     * 支付订单查询接口
     */
    public Map<String, String> payQueryOrder(Map<String, Object> requestMap) {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> params = Maps.newHashMap(requestMap);
        String sign = RSAUtils.signByPrivateKey(RSAUtils.sortString(GsonUtil.toJson(params)), hmDataApiConfig.getPrivateKey());
        params.put("sign", sign);
        if (logger.isInfoEnabled())
            logger.info(String.format("[---------------- payQueryOrder-request (%s)--------------------]\n%s", uuid, params));
        String response_str = this.httpPostAction.post(this.hmDataApiConfig.getQueryOrder(), params, uuid);
        Map<String, String> responseMap = GsonUtil.fromJsonByMap(response_str);
        verifySign(responseMap);
        return responseMap;
    }

    /**
     * 解除绑定接口
     */
    public Map<String, String> bankCardRelieve(Map<String, Object> requestMap) {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> params = Maps.newHashMap(requestMap);
        String sign = RSAUtils.signByPrivateKey(RSAUtils.sortString(GsonUtil.toJson(params)), hmDataApiConfig.getPrivateKey());
        params.put("sign", sign);
        if (logger.isInfoEnabled())
            logger.info(String.format("[---------------- bankCardRelieve-request (%s)--------------------]\n%s", uuid, params));
        String response_str = this.httpPostAction.post(this.hmDataApiConfig.getCardBindRelieve(), params, uuid);
        Map<String, String> responseMap = GsonUtil.fromJsonByMap(response_str);
        verifySign(responseMap);
        return responseMap;
    }

    /**
     * 申请提现接口
     */
    public Map<String, String> drawApply(Map<String, Object> requestMap) {
        UUID uuid = UUID.randomUUID();
        Map<String, Object> params = Maps.newHashMap(requestMap);
        String sign = RSAUtils.signByPrivateKey(RSAUtils.sortString(GsonUtil.toJson(params)), hmDataApiConfig.getPrivateKey());
        params.put("sign", sign);
        if (logger.isInfoEnabled())
            logger.info(String.format("[---------------- drawApply-request (%s)--------------------]\n%s", uuid, params));
        String response_str = this.httpPostAction.post(this.hmDataApiConfig.getDrawApply(), params, uuid);
        Map<String, String> responseMap = GsonUtil.fromJsonByMap(response_str);
        verifySign(responseMap);
        return responseMap;
    }

    /**
     * 发送短信接口
     */
    public void smsSend() {

    }

    @Autowired
    private HmDataApiConfig hmDataApiConfig;
    @Autowired
    private HttpPostAction httpPostAction;

}
