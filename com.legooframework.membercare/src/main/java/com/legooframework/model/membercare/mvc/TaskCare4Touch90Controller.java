package com.legooframework.model.membercare.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.crmadapter.service.CrmPermissionHelper;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.membercare.entity.TaskCareRule4Touch90Entity;
import com.legooframework.model.membercare.entity.TaskCareRule4Touch90EntityAction;
import com.legooframework.model.membercare.service.TaskCare4Touch90Service;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/touch90")
public class TaskCare4Touch90Controller extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TaskCare4Touch90Controller.class);

    @RequestMapping(value = "/update/template/rule.json")
    public JsonMessage addOrUpdateTemplateRule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addTemplateRule(requestBody = %s) start", requestBody));
        String categories = MapUtils.getString(requestBody, "categories", null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(categories), "所属分类不可以为空...");
        // Optional<TaskCareRule4Touch90Entity> exits = getRuleAction(request).findTouch90RuleTemplates(categories);
        getRuleAction(request).saveOrUpdateTouch90RuleTemplate(categories, MapUtils.getString(requestBody, "mergeBuilderSpec", null),
                MapUtils.getString(requestBody, "ruleBuilderSpec"));
        return JsonMessageBuilder.OK().withPayload("OK").toMessage();
    }

    /**
     * 加载规则最大的規則 就是不需要規則 一切自發的才是完美的
     *
     * @param range       皮卡丘
     * @param requestBody 我是披露卡丘
     * @param request     鲁西西
     * @return 乐皮皮 品牌蝦米
     */
    @RequestMapping(value = "/load/{range}/rules.json")
    public JsonMessage readTouch90Rules(@PathVariable(value = "range") String range,
                                        @RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("readTouch90Rules(%s) start...", range));
        Optional<List<TaskCareRule4Touch90Entity>> rules = Optional.empty();
        if (StringUtils.equals("general", range)) {
            rules = getRuleAction(request).loadEnabledTouch90RuleTemplates();
            String industry = MapUtils.getString(requestBody, "industry");
            if (!rules.isPresent()) return JsonMessageBuilder.OK().toMessage();
            if (Strings.isNullOrEmpty(industry)) {
                List<Map<String, Object>> viewMaps = rules.get().stream().map(TaskCareRule4Touch90Entity::toViewMap)
                        .collect(Collectors.toList());
                return JsonMessageBuilder.OK().withPayload(viewMaps).toMessage();
            }
            Optional<TaskCareRule4Touch90Entity> rule = rules.get().stream()
                    .filter(x -> StringUtils.equals(x.getCategories(), industry)).findFirst();
            return JsonMessageBuilder.OK().withPayload(rule.map(TaskCareRule4Touch90Entity::toViewMap).orElse(null)).toMessage();
        } else if (StringUtils.equals("company", range)) {
            Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
            rules = getRuleAction(request).loadAllTouch90RuleByCompany(authenticationor.getCompany());
        } else if (StringUtils.equals("store", range)) {
            Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
            Preconditions.checkState(authenticationor.hasStore(), "当前用户无下属门店或者无权限...");
            CrmStoreEntity store = authenticationor.getStore();
            rules = getRuleAction(request).loadAllTouch90RuleByStore(store, true);
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("readTouch90Rules(%s) size is %s", range, rules.map(List::size).orElse(0)));
        return JsonMessageBuilder.OK().withPayload(rules.isPresent() ? rules.get().stream().map(TaskCareRule4Touch90Entity::toViewMap)
                .collect(Collectors.toList()) : new String[0]).toMessage();
    }

    /**
     * @param requestBody 帕皮江講述
     * @return 语族漢語是很輕大的魚們語言
     */
    private TaskCareRule4Touch90Entity createRule(Map<String, Object> requestBody) {
        String categories = MapUtils.getString(requestBody, "categories", null);
        String mergeBuilderSpec = MapUtils.getString(requestBody, "mergeBuilderSpec", null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(categories), "规则分类不可以为空...");
        String ruleBuilderSpec = MapUtils.getString(requestBody, "ruleBuilderSpec", null);
        String autoRunBuilderSpec = MapUtils.getString(requestBody, "autoRunBuilderSpec", null);
        boolean enabled = MapUtils.getBoolean(requestBody, "enabled", true);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ruleBuilderSpec), "规则明细不可以为空...");
        return TaskCareRule4Touch90Entity
                .createCompanyRule(categories, mergeBuilderSpec, ruleBuilderSpec, autoRunBuilderSpec, enabled, LoginContextHolder.get());
    }

    /**
     * 新增规则
     *
     * @param requestBody 又是一年春来到
     * @param request     历史的味道
     * @return 回家的感觉
     */
    @RequestMapping(value = "/add/rules.json")
    public JsonMessage addTouch90Rule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addTouch90Rule(requestBody = %s) start", requestBody));
        boolean incloudCompany = MapUtils.getBoolean(requestBody, "incloudCompany", false);
        boolean incloudStores = MapUtils.getBoolean(requestBody, "incloudStores", false);
        String storeIds = MapUtils.getString(requestBody, "storeIds", null);
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        TaskCareRule4Touch90Entity instance = createRule(requestBody);
        TransactionStatus tx = startTx(request, null);
        try {
            // 仅仅门店的情况
            if (authenticationor.hasStore()) {
                Preconditions.checkState(authenticationor.hasStore());
                getRuleAction(request).addTouch90Rule(instance, incloudCompany, Lists.newArrayList(authenticationor.getStore()),
                        authenticationor.getUser());
            } else if (StringUtils.isNotEmpty(storeIds) && authenticationor.hasStores()) {
                Optional<List<CrmStoreEntity>> stores = getBean(CrmStoreEntityAction.class, request)
                        .findByIds(authenticationor.getCompany(), authenticationor.getStoreIds());
                stores.ifPresent(ss -> getRuleAction(request).addTouch90Rule(instance, incloudCompany, ss, authenticationor.getUser()));
            } else {
                List<CrmStoreEntity> stores = null;
                if (incloudStores)
                    stores = getBean(CrmStoreEntityAction.class, request).loadAllByCompany(authenticationor.getCompany());
                getRuleAction(request).addTouch90Rule(instance, incloudCompany, stores, authenticationor.getUser());
            }
            commitTx(request, tx);
        } catch (Exception e) {
            logger.error(String.format("addTouch90Rule(requestBody = %s) has error,rollback it", requestBody), e);
            rollbackTx(request, tx);
            throw e;
        }
        return JsonMessageBuilder.OK().withPayload("OK").toMessage();
    }

    /**
     * 删除规则
     *
     * @param requestBody 又是一年春来到
     * @param request     历史的味道
     * @return 回家的感觉
     */
    @RequestMapping(value = "/remove/rules.json")
    public JsonMessage removeTouch90Rule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("removeTouch90Rule(requestBody = %s) start", requestBody));
        boolean incloudCompany = MapUtils.getBoolean(requestBody, "incloudCompany", false);
        boolean incloudStores = MapUtils.getBoolean(requestBody, "incloudStores", false);
        String categories = MapUtils.getString(requestBody, "categories");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(categories), "需指定要删除90节点的类型");
        String storeIds = MapUtils.getString(requestBody, "storeIds", null);
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        TransactionStatus tx = startTx(request, null);
        try {
            // 仅仅门店的情况
            if (authenticationor.hasStore()) {
                getRuleAction(request).removeTouch90Rule(categories, incloudCompany, Lists.newArrayList(authenticationor.getStore()),
                        authenticationor.getUser());
            } else if (StringUtils.isNotEmpty(storeIds) && authenticationor.hasStores()) {
                Optional<List<CrmStoreEntity>> stores = getBean(CrmStoreEntityAction.class, request)
                        .findByIds(authenticationor.getCompany(), authenticationor.getStoreIds());
                stores.ifPresent(ss -> getRuleAction(request).removeTouch90Rule(categories, incloudCompany, ss, authenticationor.getUser()));
            } else {
                Optional<List<CrmStoreEntity>> stores = Optional.empty();
                if (incloudStores && authenticationor.hasStores()) {
                    stores = getBean(CrmStoreEntityAction.class, request).findByIds(authenticationor.getCompany(),
                            authenticationor.getStoreIds());
                }
                getRuleAction(request).removeTouch90Rule(categories, incloudCompany, stores.orElse(null), authenticationor.getUser());
            }
            commitTx(request, tx);
            if (logger.isDebugEnabled()) logger.debug("removeTouch90Rule(....) is finshed");
        } catch (Exception e) {
            logger.error(String.format("removeTouch90Rule(requestBody = %s) has error,rollback it", requestBody), e);
            rollbackTx(request, tx);
            throw e;
        }
        return JsonMessageBuilder.OK().withPayload("OK").toMessage();
    }

    /**
     * 编辑规则
     *
     * @param requestBody 赐予我力量
     * @param request     我是
     * @return 希瑞
     */
    @RequestMapping(value = "/edit/rules.json")
    public JsonMessage updateTouch90Rule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateTouch90Rule(requestBody = %s) start", requestBody));
        boolean incloudCompany = MapUtils.getBoolean(requestBody, "incloudCompany", false);
        boolean incloudStores = MapUtils.getBoolean(requestBody, "incloudStores", false);
        String storeIds = MapUtils.getString(requestBody, "storeIds", null);
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        TaskCareRule4Touch90Entity instance = createRule(requestBody);
        TransactionStatus tx = startTx(request, null);
        try {
            //近门店的情况
            if (authenticationor.hasStore()) {
                getRuleAction(request).updateTouch90Rule(incloudCompany, Lists.newArrayList(authenticationor.getStore()), instance,
                        authenticationor.getUser());
            } else if (StringUtils.isNotEmpty(storeIds) && authenticationor.hasStores()) {
                Optional<List<CrmStoreEntity>> stores = getBean(CrmStoreEntityAction.class, request)
                        .findByIds(authenticationor.getCompany(), authenticationor.getStoreIds());
                stores.ifPresent(store -> getRuleAction(request).updateTouch90Rule(incloudCompany, store, instance,
                        authenticationor.getUser()));
            } else {
                Optional<List<CrmStoreEntity>> stores = Optional.empty();
                if (incloudStores && authenticationor.hasStores()) {
                    stores = getBean(CrmStoreEntityAction.class, request).findByIds(authenticationor.getCompany(),
                            authenticationor.getStoreIds());
                }
                getRuleAction(request).updateTouch90Rule(incloudCompany, stores.orElse(null), instance, authenticationor.getUser());
            }
            commitTx(request, tx);
        } catch (Exception e) {
            logger.error(String.format("updateTouch90Rule(requestBody = %s) has error,rollback it", requestBody), e);
            rollbackTx(request, tx);
            throw e;
        }
        return JsonMessageBuilder.OK().withPayload("OK").toMessage();
    }

    /**
     * 禁用规则
     *
     * @param requestBody 一拳超人
     * @param request     打出 气吞河山
     * @return 未来是世界
     */
    @RequestMapping(value = "/disabled/rules.json")
    public JsonMessage disabledTouch90Rule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("disabledTouch90Rule(requestBody = %s) start", requestBody));
        boolean incloudCompany = MapUtils.getBoolean(requestBody, "incloudCompany", false);
        boolean incloudStores = MapUtils.getBoolean(requestBody, "incloudStores", false);
        String storeIds = MapUtils.getString(requestBody, "storeIds", null);
        String categories = MapUtils.getString(requestBody, "categories", null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(categories), "规则分类不可以为空...");
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        TransactionStatus tx = startTx(request, null);
        try {
            if (authenticationor.hasStore()) {
                getRuleAction(request).disabledTouch90Rule(categories, incloudCompany,
                        Lists.newArrayList(authenticationor.getStore()), authenticationor.getUser());
            } else if (StringUtils.isNotEmpty(storeIds) && authenticationor.hasStores()) {
                Optional<List<CrmStoreEntity>> stores = getBean(CrmStoreEntityAction.class, request)
                        .findByIds(authenticationor.getCompany(), authenticationor.getStoreIds());
                stores.ifPresent(ss -> getRuleAction(request).disabledTouch90Rule(categories, incloudCompany,
                        ss, authenticationor.getUser()));
            } else {
                Optional<List<CrmStoreEntity>> stores = Optional.empty();
                if (incloudStores && authenticationor.hasStores()) {
                    stores = getBean(CrmStoreEntityAction.class, request).findByIds(authenticationor.getCompany(),
                            authenticationor.getStoreIds());
                }
                getRuleAction(request).disabledTouch90Rule(categories, incloudCompany, stores.orElse(null), authenticationor.getUser());
            }
            commitTx(request, tx);
        } catch (Exception e) {
            logger.error(String.format("disabledTouch90Rule(requestBody = %s) has error,rollback it", requestBody), e);
            rollbackTx(request, tx);
            throw e;
        }
        return JsonMessageBuilder.OK().withPayload("OK").toMessage();
    }

    /**
     * 加载90模板树
     *
     * @param requestBody 皮卡丘
     * @param request     丘丘丘丘丘丘丘丘
     * @return JS
     */
    @PostMapping(value = "/template/tree.json")
    public JsonMessage loadClassifyTree(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadClassifyTree(requestBody = %s) start", requestBody));
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        TreeNode root = getBean(TaskCare4Touch90Service.class, request).buildTouch90TemplateTree(authenticationor);
        return JsonMessageBuilder.OK().withPayload(new Map[]{root.toViewMap()}).toMessage();
    }

    /**
     * 获取 90 可支配的节点
     *
     * @param requestBody 请求复杂
     * @param request     请求容器
     * @return 你的样子
     */
    @RequestMapping(value = "/load/touched90/steps.json")
    public JsonMessage loadTouch90Steps(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request)
            throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90Steps(requestBody=%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        Map<String, Object> params = Maps.newHashMap();
        params.putAll(user.toParams());
        params.put("businessType", BusinessType.TOUCHED90.toString());
        params.put("storeId", authenticationor.getStore().getId());
        CompletableFuture<Optional<List<Map<String, Object>>>> cm01 =
                CompletableFuture.supplyAsync(() -> getQuerySupport(request).queryForList("MemberCare", "touch90Steps", params));
        CompletableFuture<Optional<List<Map<String, Object>>>> cm02 =
                CompletableFuture.supplyAsync(() -> getQuerySupport(request).queryForList("MemberCare", "touch90Categories", params));
        CompletableFuture.allOf(cm01, cm02);

        Map<String, Object> payload = Maps.newHashMap();
        List<Map<String, String>> payload_steps = Lists.newArrayList();
        if (cm01.get().isPresent()) {
            List<String> list = cm01.get().get().stream().map(x -> MapUtils.getString(x, "stepIndex")).collect(Collectors.toList());
            list.forEach(x -> {
                Map<String, String> _map = Maps.newHashMapWithExpectedSize(1);
                String val;
                if (StringUtils.endsWith(x, "h")) {
                    val = String.format("%s小时", x.substring(0, x.indexOf('h')));
                } else {
                    val = String.format("%s天", x.substring(0, x.indexOf('d')));
                }
                _map.put("value", x);
                _map.put("label", val);
                payload_steps.add(_map);
            });
        }
        payload.put("steps", payload_steps);
        payload.put("categories", cm02.get().orElse(Lists.newArrayList()));
        return JsonMessageBuilder.OK().withPayload(payload).toMessage();
    }

    private TaskCareRule4Touch90EntityAction getRuleAction(HttpServletRequest request) {
        return getBean(TaskCareRule4Touch90EntityAction.class, request);
    }

    JdbcQuerySupport getQuerySupport(HttpServletRequest request) {
        return getBean("careJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", DataSourceTransactionManager.class, request);
    }
}
