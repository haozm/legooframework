package com.legooframework.model.membercare.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.membercare.entity.CareRuleEntityAction;
import com.legooframework.model.membercare.entity.TaskSwitchEntity;
import com.legooframework.model.membercare.entity.TaskSwitchEntityAction;
import com.legooframework.model.membercare.entity.Touch90CareRuleEntity;
import com.legooframework.model.membercare.service.MemerCareJobService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/care")
public class MemberCareController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MemberCareController.class);

    @RequestMapping(value = "/read/touch90/switch.json")
    public JsonMessage loadTouch90Switch(HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90Switch(url:%s)", request.getRequestURI()));
        Optional<List<TaskSwitchEntity>> taskSwitch = getBean(TaskSwitchEntityAction.class, request).queryAllTouch90Switch();
        return taskSwitch.isPresent() ? JsonMessageBuilder.OK()
                .withPayload(taskSwitch.get().stream().map(TaskSwitchEntity::toViewMap).collect(Collectors.toList())).toMessage()
                : JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/update/touch90/switch.json")
    public JsonMessage saveOrUpdate90Switch(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("saveOrUpdate90Switch(url:%s,requestBody = %s )", request.getRequestURI(), requestBody));
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "公司ID=%s 尚未注册到系统...", companyId);
        Preconditions.checkNotNull(companyId);
        boolean enabled = MapUtils.getBoolean(requestBody, "switched", true);
        getBean(TaskSwitchEntityAction.class, request).updateTouch90Switch(company.get(), enabled);
        return JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/read/touch90/rules.json")
    public JsonMessage loadTouch90Rules(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90Rules(requestBody:%s)", requestBody));
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Preconditions.checkNotNull(companyId, "入参 companyId 不可以为空....");
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "companyId = %s 对应的公司不存在...", companyId);
        Optional<CrmStoreEntity> store = storeId == null ? Optional.empty() : getBean(CrmStoreEntityAction.class, request)
                .findById(company.get(), storeId);
        Touch90CareRuleEntity touch90CareRules = getBean(CareRuleEntityAction.class, request)
                .loadRuleByStore(company.get(), store.orElse(null));
        return touch90CareRules == null ? JsonMessageBuilder.OK().toMessage() :
                JsonMessageBuilder.OK().withPayload(touch90CareRules.toViewMap()).toMessage();
    }

    @RequestMapping(value = "/update/touch90/rules.json")
    public JsonMessage saveOrUpdate90Rule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("saveOrUpdate90Rule(url=%s,requestBody=%s)", request.getRequestURL(), requestBody));
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "非法的取值 companyId = %s", companyId);
        Integer employeeId = MapUtils.getInteger(requestBody, "employeeId");
        Preconditions.checkNotNull(employeeId, "非法的取值 employeeId = %s", companyId);
        boolean enabled = MapUtils.getBoolean(requestBody, "enabled", false);
        boolean automatic = MapUtils.getBoolean(requestBody, "automatic", false);
        boolean rewrite = MapUtils.getBoolean(requestBody, "rewrite", false);
        boolean concalBefore = MapUtils.getBoolean(requestBody, "concalBefore", false);
        Integer maxConsumptionDays = MapUtils.getInteger(requestBody, "maxConsumptionDays");
        Preconditions.checkNotNull(maxConsumptionDays, "非法的取值 maxConsumptionDays = %s", maxConsumptionDays);
        Preconditions.checkState(maxConsumptionDays > 0, "非法的取值 maxConsumptionDays = %s", maxConsumptionDays);
        Integer maxAmountOfconsumption = MapUtils.getInteger(requestBody, "maxAmountOfconsumption");
        Preconditions.checkNotNull(maxAmountOfconsumption, "非法的取值 maxAmountOfconsumption = %s", maxConsumptionDays);
        Preconditions.checkState(maxAmountOfconsumption > 0, "非法的取值 maxAmountOfconsumption = %s", maxConsumptionDays);
        String ruleDetails = MapUtils.getString(requestBody, "ruleDetails");
        Preconditions.checkNotNull(ruleDetails, "规则明细不可以为空值...");
        List<Integer> storeIds = null;
        String storeIds_str = MapUtils.getString(requestBody, "storeIds");
        if (StringUtils.isNoneEmpty(storeIds_str)) {
            storeIds = Stream.of(StringUtils.split(storeIds_str, ',')).map(Integer::valueOf).collect(Collectors.toList());
        }
        getBean(MemerCareJobService.class, request).saveOrUpdate90Rule(companyId, employeeId, enabled, automatic,
                maxConsumptionDays, maxAmountOfconsumption, concalBefore, ruleDetails, rewrite, storeIds);
        return JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/touch90/detail/byid.json")
    @ResponseBody
    public JsonMessage loadTouch90Detail(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90Detail(url=%s,requestBody=%s)", request.getRequestURL(), requestBody));
        String taskId = MapUtils.getString(requestBody, "taskId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(taskId), "参数 taskId=‘’ 不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskId", taskId);
        Optional<List<Map<String, Object>>> list = getQuerySupport(request)
                .queryForList("MemberCare", "touch90_detail", params);
        if (!list.isPresent()) return JsonMessageBuilder.OK().toMessage();
        return JsonMessageBuilder.OK().withPayload(list.get()).toMessage();
    }

    @RequestMapping(value = "/load/touch90/list.json")
    public JsonMessage loadTouch90List(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90Detail(url=%s,requestBody=%s)", request.getRequestURL(), requestBody));
        Map<String, Object> params = Maps.newHashMap();
        Integer pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
        Integer pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        Integer companyId = MapUtils.getInteger(requestBody, "companyId", -1);

        params.put("companyId", companyId);
        params.put("taskType", 90);

        // 状态描述
        String taskStatus_str = MapUtils.getString(requestBody, "taskStatus", "2");
        String[] taskStatus = StringUtils.split(taskStatus_str, ',');
        params.put("taskStatus", taskStatus);

        // 是否跨店
        params.put("crossStoreFlag", false);
        Integer crossStore = MapUtils.getInteger(requestBody, "crossStore", -1);
        if (crossStore == 1 || crossStore == 0) {
            params.put("crossStore", crossStore);
            params.put("crossStoreFlag", true);
        }

        // 是否独立
        boolean single = MapUtils.getBoolean(requestBody, "single");
        params.put("single", true);
        if (single) {
            Integer storeId = MapUtils.getInteger(requestBody, "storeId", -1);
            params.put("storeId", storeId);
        } else {
            String[] storeIds = StringUtils.split(MapUtils.getString(requestBody, "storeIds"));
            List<Integer> list = Stream.of(storeIds).map(Integer::valueOf).collect(Collectors.toList());
            params.put("storeIds", list);
        }

        PagingResult pg = getQuerySupport(request)
                .queryForPage("MemberCare", "touch90_list", pageNum, pageSize, params);
        return JsonMessageBuilder.OK().withPayload(pg.toData()).toMessage();
    }

    JdbcQuerySupport getQuerySupport(HttpServletRequest request) {
        return getBean("careJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}

