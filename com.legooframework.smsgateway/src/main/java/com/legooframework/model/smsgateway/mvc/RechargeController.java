package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.smsgateway.entity.RechargeBalanceEntity;
import com.legooframework.model.smsgateway.entity.RechargeBalanceEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeTreeDto;
import com.legooframework.model.smsgateway.entity.RechargeType;
import com.legooframework.model.smsgateway.service.BundleService;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/recharge")
public class RechargeController extends SmsBaseController {

    private static final Logger logger = LoggerFactory.getLogger(RechargeController.class);

    @PostMapping(value = "/tree.json")
    public JsonMessage loadRechargeTree(@RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadRechargeTree(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            OrgEntity company = loadCompanyById(companyId, request);
            RechargeTreeDto treeRoot = new RechargeTreeDto(company);
            Optional<List<RechargeBalanceEntity>> node_list_opt = getBean(RechargeBalanceEntityAction.class, request)
                    .findAllStoreGroupBalance(company);
            if (node_list_opt.isPresent()) {
                for (RechargeBalanceEntity $it : node_list_opt.get()) {
                    RechargeTreeDto node = new RechargeTreeDto($it, treeRoot.getId());
                    treeRoot.addChild(node);
                    Optional<List<StoEntity>> stores = getBean(StoEntityAction.class, request).findByIds($it.getStoreIds());
                    stores.ifPresent(x -> x.forEach(s -> {
                        node.addChild(new RechargeTreeDto(s, $it.getId()));
                    }));
                }
            }
            return JsonMessageBuilder.OK().withPayload(new Object[]{treeRoot.toMap()}).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/tree/add.json")
    public JsonMessage addRechargeTree(@RequestBody(required = false) Map<String, Object> requestBody,
                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("editRechargeTree(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            String groupName = MapUtils.getString(requestBody, "groupName", null);
            String storeIds_raw = MapUtils.getString(requestBody, "storeIds", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(groupName), "分组名称[groupName]不可以为空...");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds_raw), "待分组的门店[storeIds]不可以为空...");
            OrgEntity company = loadCompanyById(companyId, request);
            List<Integer> storeIds = Splitter.on(',').splitToList(storeIds_raw).stream()
                    .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
            Optional<List<StoEntity>> stores = getBean(StoEntityAction.class, request).findByIds(storeIds);
            Preconditions.checkState(stores.isPresent(), "给定的列表无合法的门店....");
            getBean(RechargeBalanceEntityAction.class, request).createStoreGroupBalance(company, stores.get(), groupName);
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/tree/edit.json")
    public JsonMessage editRechargeTree(@RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("editRechargeTree(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Integer action = MapUtils.getInteger(requestBody, "action", -1);
            String nodeId = MapUtils.getString(requestBody, "nodeId");
            String storeIds_raw = MapUtils.getString(requestBody, "storeIds", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds_raw), "待分组的门店[storeIds]不可以为空...");
            List<Integer> storeIds = Splitter.on(',').splitToList(storeIds_raw).stream()
                    .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
            Optional<List<StoEntity>> stores = getBean(StoEntityAction.class, request).findByIds(storeIds);
            Preconditions.checkState(stores.isPresent(), "给定的列表无合法的门店....");
            getBean(RechargeBalanceEntityAction.class, request).editStoreGroupBalance(nodeId, action, stores.get());
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }
    
    @PostMapping(value = "/action.json")
    public JsonMessage rechargeAction(@RequestBody(required = false) Map<String, Object> requestBody,
                                      HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("recharge(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "公司Id不可以为空值...");
        String storeIds = MapUtils.getString(requestBody, "storeIds", null);
        Integer storeId = null;
        if (Strings.isNullOrEmpty(storeIds)) {
            storeId = MapUtils.getInteger(requestBody, "storeId", -1);
        }
        String type = MapUtils.getString(requestBody, "type");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "入参 type 不可以为空...");
        RechargeType rechargeType = Enum.valueOf(RechargeType.class, type);
        double unitPrice = MapUtils.getDoubleValue(requestBody, "unitPrice", 0.0D);
        String remarke = MapUtils.getString(requestBody, "remarke");
        int rechargeAmount = MapUtils.getIntValue(requestBody, "rechargeAmount");
        int totalQuantity = MapUtils.getIntValue(requestBody, "totalQuantity");
        RechargeReqDto rechargeDto = new RechargeReqDto(companyId, storeId, storeIds, rechargeType, unitPrice,
                rechargeAmount, totalQuantity, remarke);
        Message<RechargeReqDto> msg_request = MessageBuilder.withPayload(rechargeDto)
                .setHeader("user", user)
                .setHeader("action", "recharge")
                .build();
        Message<?> message_rsp = getMessagingTemplate(request).sendAndReceive(BundleService.CHANNEL_SMS_BILLING, msg_request);
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
