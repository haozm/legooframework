package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.smsgateway.entity.RechargeType;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/recharge")
public class RechargeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RechargeController.class);

    @PostMapping(value = "/{channel}/recharge.json")
    public JsonMessage recharge(@PathVariable(value = "channel") String channel,
                                @RequestBody(required = false) Map<String, Object> requestBody,
                                HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("recharge(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        Preconditions.checkArgument(ArrayUtils.contains(CHANNELS, channel), "非法的取值 %s,取值范围为：%s", channel, CHANNELS);
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "公司Id不可以为空值...");
        String storeGroupId = MapUtils.getString(requestBody, "storeGroupId", null);
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        String rechargeType_str = MapUtils.getString(requestBody, "rechargeType");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(rechargeType_str), "入参 rechargeType 不可以为空...");
        RechargeType rechargeType = Enum.valueOf(RechargeType.class, rechargeType_str);
        double unitPrice = MapUtils.getDoubleValue(requestBody, "unitPrice", 0.0D);
        String remarke = MapUtils.getString(requestBody, "remarke");
        int rechargeAmount = MapUtils.getIntValue(requestBody, "rechargeAmount");
        int totalQuantity = MapUtils.getIntValue(requestBody, "totalQuantity");
        LoginContext user = LoginContextHolder.get();
        RechargeReqDto rechargeDto = new RechargeReqDto(companyId, storeId, storeGroupId, rechargeType, unitPrice,
                rechargeAmount, totalQuantity, remarke);
        Message<RechargeReqDto> msg_request = MessageBuilder.withPayload(rechargeDto)
                .setHeader("user", user)
                .build();
        Message<?> message_rsp = getMessagingTemplate(request).sendAndReceive(msg_request);
        if (message_rsp.getPayload() instanceof Exception)
            throw (Exception) message_rsp.getPayload();
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/{channel}/balance.json")
    public JsonMessage rechargelance(@PathVariable(value = "channel") String channel,
                                     @RequestBody(required = false) Map<String, Object> requestBody,
                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("recharge(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        Preconditions.checkArgument(ArrayUtils.contains(CHANNELS, channel), "非法的取值 %s,取值范围为：%s", channel, CHANNELS);
        Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
        int storeId = MapUtils.getIntValue(requestBody, "storeId", -1);
        String storeGroupId = MapUtils.getString(requestBody, "storeGroupId", "NONE");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("storeGroupId", storeGroupId);
        Map<String, Object> res = Maps.newHashMap();
        Optional<Long> size = getQueryEngine(request)
                .queryForObject("RechargeDetailEntity", "loadBalanceByInstance", params, Long.class);
        res.put("balance", size.orElse(0L));
        return JsonMessageBuilder.OK().withPayload(res).toMessage();
    }

    @PostMapping(value = "/{channel}/{range}/detail.json")
    public JsonMessage rechargeDetails4Manager(@PathVariable(value = "channel") String channel,
                                               @PathVariable(value = "range") String range,
                                               @RequestBody(required = false) Map<String, Object> requestBody,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeDetails4Manager(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        Preconditions.checkArgument(ArrayUtils.contains(CHANNELS, channel), "非法的取值 %s,取值范围为：%s", channel, CHANNELS);
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
        Preconditions.checkArgument(StringUtils.containsAny(range, "all", "company"), "非法的参数请求%s", range);
        Map<String, Object> params = Maps.newHashMap(requestBody);
        if (StringUtils.equals("company", range)) {
            LoginContext user = LoginContextHolder.get();
            params.put("companyId", user.getTenantId().intValue());
            if (user.isStoreManager()) {
                params.put("storeId", user.getStoreId());
            }
        }
        PagingResult page = getQueryEngine(request).queryForPage("RechargeDetailEntity", "list", pageNum, pageSize, params);
        return JsonMessageBuilder.OK().withPayload(page.toData()).toMessage();
    }

    JdbcQuerySupport getQueryEngine(HttpServletRequest request) {
        return getBean(JdbcQuerySupport.class, request);
    }

    MessagingTemplate getMessagingTemplate(HttpServletRequest request) {
        return getBean("smsMessagingTemplate", MessagingTemplate.class, request);
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", PlatformTransactionManager.class, request);
    }

}
