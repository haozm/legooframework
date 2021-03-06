package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.smsgateway.entity.*;
import com.legooframework.model.smsgateway.service.BundleService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController(value = "rechargeController")
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

    @PostMapping(value = "/tree/node/add.json")
    public JsonMessage addRechargeTreeNode(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("editRechargeTree(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            String groupName = MapUtils.getString(requestBody, "groupName", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(groupName), "分组名称[groupName]不可以为空...");
            OrgEntity company = loadCompanyById(companyId, request);
            Optional<List<StoEntity>> stores = Optional.empty();
            String storeIds_raw = MapUtils.getString(requestBody, "storeIds", null);
            if (!Strings.isNullOrEmpty(storeIds_raw)) {
                List<Integer> storeIds = Splitter.on(',').splitToList(storeIds_raw).stream()
                        .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
                stores = getBean(StoEntityAction.class, request).findByIds(storeIds);
            }
            getBean(RechargeBalanceEntityAction.class, request).createStoreGroupBalance(company, stores.orElse(null), groupName);
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/tree/node/edit.json")
    public JsonMessage editRechargeTreeNode(@RequestBody(required = false) Map<String, Object> requestBody,
                                            HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("editRechargeTree(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Integer action = MapUtils.getInteger(requestBody, "action", 2);
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            OrgEntity company = loadCompanyById(companyId, request);
            String nodeId = MapUtils.getString(requestBody, "nodeId");
            String storeIds_raw = MapUtils.getString(requestBody, "storeIds", null);
            Optional<List<StoEntity>> stores = Optional.empty();
            if (ArrayUtils.contains(new int[]{0, 1}, action)) {
                Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds_raw), "待编辑的门店[storeIds]不可以为空...");
                List<Integer> storeIds = Splitter.on(',').splitToList(storeIds_raw).stream()
                        .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
                stores = getBean(StoEntityAction.class, request).findByIds(storeIds);
                Preconditions.checkState(stores.isPresent(), "给定的列表无合法的门店....");
            }
            getBean(RechargeBalanceEntityAction.class, request).editStoreGroupBalance(company, nodeId, action, stores.orElse(null));
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/action.json")
    public JsonMessage rechargeAction(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeAction(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
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
            int rechargeAmount = MapUtils.getIntValue(requestBody, "rechargeAmount", 0);
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
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/balance/total.json")
    public JsonMessage rechargeBlanceTotal(@RequestBody(required = false) Map<String, Object> requestBody,
                                           HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeBlanceTotal(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            OrgEntity company = loadCompanyById(companyId, request);
            int scope = MapUtils.getInteger(requestBody, "scope", -1);
            RechargeScope rechargeScope = RechargeScope.paras(scope);
            String nodeId = MapUtils.getString(requestBody, "nodeId");
            Map<String, Object> params = company.toParamMap();
            params.put("rechargeScope", rechargeScope.getScope());
            if (RechargeScope.StoreGroup == rechargeScope) {
                params.put("storeIds", nodeId);
            } else if (RechargeScope.Store == rechargeScope) {
                params.put("storeId", Integer.parseInt(nodeId));
            }
            Map<String, Object> res = Maps.newHashMap();
            Optional<Long> size = getQueryEngine(request).queryForObject("RechargeDetailEntity", "loadBalanceByInstance", params, Long.class);
            res.put("balance", size.orElse(0L));
            return JsonMessageBuilder.OK().withPayload(res).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @PostMapping(value = "/balance/detail.json")
    public JsonMessage rechargeDetails4Manager(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("rechargeDetails4Manager(url=%s,requestBody= %s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 20);
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            Map<String, Object> params = user.toViewMap();
            params.put("companyId", companyId);
            params.putAll(requestBody);
            PagingResult page = getQueryEngine(request).queryForPage("RechargeDetailEntity", "rechargeDetail", pageNum, pageSize, params);
            return JsonMessageBuilder.OK().withPayload(page.toData()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    private JdbcQuerySupport getQueryEngine(HttpServletRequest request) {
        return getBean("smsJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

    private MessagingTemplate getMessagingTemplate(HttpServletRequest request) {
        return getBean("smsMessagingTemplate", MessagingTemplate.class, request);
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", PlatformTransactionManager.class, request);
    }

}
