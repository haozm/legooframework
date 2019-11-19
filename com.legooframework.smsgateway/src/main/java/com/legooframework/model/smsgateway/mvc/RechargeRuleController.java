package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.smsgateway.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@RestController(value = "smsgatewayRechargeRuleController")
@RequestMapping(value = "/recharge/rule")
public class RechargeRuleController extends SmsBaseController {

    private static final Logger logger = LoggerFactory.getLogger(RechargeRuleController.class);

    private static Comparator<RechargeRuleEntity> order = Comparator
            .comparingInt((ToIntFunction<RechargeRuleEntity>) o -> o.isEnabled() ? 0 : 1)
            .thenComparingInt(o -> o.isGlobalRule() ? 1 : 0)
            .thenComparingInt(o -> o.isNotExpired() ? 0 : 1);

    /**
     * 加载 全部列表(或则指定公司的列表)
     *
     * @param requestBody req
     * @param request     req
     * @return map
     */
    @PostMapping(value = "/all/list.json")
    public JsonMessage loadAllRules(@RequestBody(required = false) Map<String, Object> requestBody,
                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllRules(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            OrgEntity company = null;
            if (companyId != -1) {
                company = loadCompanyById(companyId, request);
            }
            Optional<List<RechargeRuleEntity>> rulesOpt = getBean(RechargeRuleEntityAction.class, request).loadAllRules();
            if (!rulesOpt.isPresent()) return JsonMessageBuilder.OK().toMessage();
            List<RechargeRuleEntity> rules = rulesOpt.get();
            final OrgEntity com = company;
            rules = com == null ? rules.stream().filter(RechargeRuleEntity::isGlobalRule).collect(Collectors.toList()) :
                    rules.stream().filter(x -> x.isOwnerCompany(com)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(rules)) return JsonMessageBuilder.OK().toMessage();
            rules.sort(order);
            List<Map<String, Object>> resList = Lists.newArrayList();

            rules.forEach(r -> {
                Map<String, Object> map = r.toViewMap();
                if (null != com && !r.isGlobalRule()) map.put("companyName", com.getName());
                resList.add(map);
            });
            return JsonMessageBuilder.OK().withPayload(resList).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 加载 全部列表(或则指定公司的列表)
     *
     * @param requestBody req
     * @param request     req
     * @return map
     */
    @PostMapping(value = "/enabled/list.json")
    public JsonMessage loadAllEnabledRules(@RequestBody(required = false) Map<String, Object> requestBody,
                                           HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllRules(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
            Preconditions.checkArgument(companyId > 0, "参数 companyId =%s 非法", companyId);
            OrgEntity company = loadCompanyById(companyId, request);
            Optional<List<RechargeRuleEntity>> rulesOpt = getBean(RechargeRuleEntityAction.class, request).loadAllRules();
            if (!rulesOpt.isPresent()) return JsonMessageBuilder.OK().toMessage();
            List<RechargeRuleEntity> rules = rulesOpt.get();
            rules = rules.stream().filter(x -> x.isOwnerCompany(company))
                    .filter(RechargeRuleEntity::isEnabled).filter(RechargeRuleEntity::isNotExpired)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(rules)) return JsonMessageBuilder.OK().toMessage();
            rules.sort(order);
            List<Map<String, Object>> resList = Lists.newArrayList();
            rules.forEach(r -> {
                Map<String, Object> map = r.toViewMap();
                if (!r.isGlobalRule()) map.put("companyName", company.getName());
                resList.add(map);
            });
            return JsonMessageBuilder.OK().withPayload(resList).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 加载 全部列表(或则指定公司的列表)
     *
     * @param requestBody req
     * @param request     req
     * @return map
     */
    @PostMapping(value = "/state/change.json")
    public JsonMessage changeState(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("changeState(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            String ruleId = MapUtils.getString(requestBody, "ruleId");
            boolean state = MapUtils.getBoolean(requestBody, "state");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(ruleId), "请指定待修改规则的ID值...");
            if (state) {
                getBean(RechargeRuleEntityAction.class, request).enabled(ruleId);
            } else {
                getBean(RechargeRuleEntityAction.class, request).disabled(ruleId);
            }
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * 加载 全部列表(或则指定公司的列表)
     *
     * @param requestBody req
     * @param request     req
     * @return map
     */
    @PostMapping(value = "/action/add.json")
    public JsonMessage addRule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addRule(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            Long minVal = MapUtils.getLong(requestBody, "min");
            Long maxVal = MapUtils.getLong(requestBody, "max");
            Preconditions.checkArgument((minVal != null && minVal > 0) && (maxVal != null && maxVal > 0) && maxVal > minVal,
                    "非法的入参[min=%s,max=%s]", minVal, maxVal);
            double unitPrice = MapUtils.getDouble(requestBody, "unitPrice");
            Preconditions.checkArgument(unitPrice > 0, "短信单价值%s 非法...", unitPrice);
            OrgEntity company = null;
            Integer companyId = MapUtils.getInteger(requestBody, "companyId");
            if (null != companyId) {
                company = loadCompanyById(companyId, request);
            }
            String remarks = MapUtils.getString(requestBody, "remarks");
            LocalDate expiredDate = null;
            String _expiredDate = MapUtils.getString(requestBody, "expiredDate");
            if (!Strings.isNullOrEmpty(_expiredDate)) {
                expiredDate = DateTimeUtils.parseYYYYMMDD(_expiredDate);
            }
            getBean(RechargeRuleEntityAction.class, request).addRule(minVal * 100, maxVal * 100, unitPrice,
                    company, remarks, expiredDate);
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

}
