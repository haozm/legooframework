package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.smsgateway.entity.RechargeRuleEntity;
import com.legooframework.model.smsgateway.entity.RechargeRuleEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/rechargerule")
public class RechargeRuleController extends BaseController {

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
    @PostMapping(value = "/load/all.json")
    public JsonMessage loadAllRules(@RequestBody(required = false) Map<String, Object> requestBody,
                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllRules(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setCtx(getLoginContext());
        Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);
        CrmOrganizationEntity company = null;
        if (companyId != -1) {
            Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                    .findCompanyById(companyId);
            Preconditions.checkState(companyOpt.isPresent(), "不存在Id=%s 对应的公司...", companyId);
            company = companyOpt.get();
        }
        Optional<List<RechargeRuleEntity>> rulesOpt = getBean(RechargeRuleEntityAction.class, request).loadAllRules();
        if (!rulesOpt.isPresent()) return JsonMessageBuilder.OK().toMessage();
        List<RechargeRuleEntity> rules = rulesOpt.get();
        final CrmOrganizationEntity com = company;
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
    }

    /**
     * 加载 全部列表(或则指定公司的列表)
     *
     * @param requestBody req
     * @param request     req
     * @return map
     */
    @PostMapping(value = "/load/enabled.json")
    public JsonMessage loadAllEnabledRules(@RequestBody(required = false) Map<String, Object> requestBody,
                                           HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllRules(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setCtx(getLoginContext());
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                .findCompanyById(companyId);
        Preconditions.checkState(companyOpt.isPresent(), "不存在Id=%s 对应的公司...", companyId);
        Optional<List<RechargeRuleEntity>> rulesOpt = getBean(RechargeRuleEntityAction.class, request).loadAllRules();
        if (!rulesOpt.isPresent()) return JsonMessageBuilder.OK().toMessage();
        List<RechargeRuleEntity> rules = rulesOpt.get();
        rules = rules.stream().filter(x -> x.isOwnerCompany(companyOpt.get()))
                .filter(RechargeRuleEntity::isEnabled).filter(RechargeRuleEntity::isNotExpired)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rules)) return JsonMessageBuilder.OK().toMessage();
        rules.sort(order);
        List<Map<String, Object>> resList = Lists.newArrayList();
        rules.forEach(r -> {
            Map<String, Object> map = r.toViewMap();
            if (!r.isGlobalRule()) map.put("companyName", companyOpt.get().getName());
            resList.add(map);
        });
        return JsonMessageBuilder.OK().withPayload(resList).toMessage();
    }

    /**
     * 加载 全部列表(或则指定公司的列表)
     *
     * @param requestBody req
     * @param request     req
     * @return map
     */
    @PostMapping(value = "/{state}/change.json")
    public JsonMessage changeState(@PathVariable(value = "state") String state,
                                   @RequestBody Map<String, Object> requestBody,
                                   HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("changeState(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setCtx(getLoginContext());
        String ruleId = MapUtils.getString(requestBody, "ruleId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ruleId), "请指定待修改规则的ID值...");
        if (StringUtils.equalsIgnoreCase("true", state)) {
            getBean(RechargeRuleEntityAction.class, request).enabled(ruleId);
        } else {
            getBean(RechargeRuleEntityAction.class, request).disabled(ruleId);
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 加载 全部列表(或则指定公司的列表)
     *
     * @param requestBody req
     * @param request     req
     * @return map
     */
    @PostMapping(value = "/add/rule.json")
    public JsonMessage addRule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addRule(url=%s,param=%s)", request.getRequestURL(), requestBody));
        LoginContextHolder.setCtx(getLoginContext());
        Long minVal = MapUtils.getLong(requestBody, "min");
        Long maxVal = MapUtils.getLong(requestBody, "max");
        Preconditions.checkArgument((minVal != null && minVal > 0) && (maxVal != null && maxVal > 0) && maxVal > minVal,
                "非法的入参[min=%s,max=%s]", minVal, maxVal);
        double unitPrice = MapUtils.getDouble(requestBody, "unitPrice");
        Preconditions.checkArgument(unitPrice > 0, "短信单价值%s 非法...", unitPrice);
        CrmOrganizationEntity company = null;
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        if (null != companyId) {
            Optional<CrmOrganizationEntity> comOpt = getBean(CrmOrganizationEntityAction.class, request).findCompanyById(companyId);
            Preconditions.checkState(comOpt.isPresent(), "Id=%s对应的公司不存在...", companyId);
            company = comOpt.get();
        }
        String remarks = MapUtils.getString(requestBody, "remarks");
        LocalDate expiredDate = null;
        String _expiredDate = MapUtils.getString(requestBody, "expiredDate");
        if (!Strings.isNullOrEmpty(_expiredDate)) {
            expiredDate = DateTimeUtils.parseYYYYMMDD(_expiredDate);
        }
//        Long min, Long max, double unitPrice, CrmOrganizationEntity company,
//                String remarks, LocalDate expiredDate
        getBean(RechargeRuleEntityAction.class, request).addRule(minVal * 100, maxVal * 100, unitPrice,
                company, remarks, expiredDate);
        return JsonMessageBuilder.OK().toMessage();
    }

}
