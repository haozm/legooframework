package com.legooframework.model.takecare.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.takecare.entity.CareNinetyRuleEntity;
import com.legooframework.model.takecare.entity.CareNinetyRuleEntityAction;
import com.legooframework.model.takecare.entity.Constant;
import com.legooframework.model.takecare.service.CareNinetyTaskAgg;
import com.legooframework.model.takecare.service.TakeCareService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController(value = "takecareMvcController")
@RequestMapping(value = "/takecare")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("takeCareBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    @RequestMapping(value = "/cache/clean.json")
    @ResponseBody
    public JsonMessage cacheClean(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("cacheClean(url=%s)", request.getRequestURI()));
        LoginContextHolder.setAnonymousCtx();
        String cacheName = request.getParameter("cache");
        if (Strings.isNullOrEmpty(cacheName)) cacheName = "all";
        getBean(Constant.CACHE_MANAGER, CaffeineCacheManager.class, request).clearByCache(Constant.CACHE_ENTITYS);
        LoginContextHolder.clear();
        return JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/membercare/bymember/pages.json")
    @ResponseBody
    public JsonMessage loadMemberCareByMember(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadMemberCareByMember(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        int pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Integer memberId = MapUtils.getInteger(requestBody, "memberId", 0);
            Map<String, Object> params = user.toViewMap();
            params.put("memberId", memberId);
            PagingResult paged = getJdbcQuery(request)
                    .queryForPage("MemberCareRecord", "memberCareByMember", pageNum, pageSize, params);
            return JsonMessageBuilder.OK().withPayload(paged.toData()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/birthday/executecare.json")
    @ResponseBody
    public JsonMessage executeBirthdayCare(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("executeBirthdayCare(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        TransactionStatus ts = null;
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            String type = MapUtils.getString(requestBody, "type", null);
            List<Integer> memberIds = null;
            if (StringUtils.equalsAnyIgnoreCase("byparams", type)) {
                Map<String, Object> params = user.toViewMap();
                if (MapUtils.isNotEmpty(requestBody)) params.putAll(requestBody);
                Optional<List<Integer>> membeIds_opt = getBean(CovariantService.class, request).loadMemberIds(params, user);
                Preconditions.checkState(membeIds_opt.isPresent(), "无匹配的会员");
                return JsonMessageBuilder.OK().withPayload(membeIds_opt.get()).toMessage();
                // memberIds = membeIds_opt.get();
            } else {
                String mIds = MapUtils.getString(requestBody, "memberIds", null);
                Preconditions.checkArgument(!Strings.isNullOrEmpty(mIds), "待发送的人员ID不可以为空...");
                memberIds = Stream.of(StringUtils.split(mIds, ',')).mapToInt(Integer::parseInt)
                        .boxed().collect(Collectors.toList());
            }
            String followUpContent = MapUtils.getString(requestBody, "followUpContent", null);
            String followUpWays = MapUtils.getString(requestBody, "followUpWays", null);
            String imgUrls = MapUtils.getString(requestBody, "imgUrls", null);
            String[] arg_imgs = Strings.isNullOrEmpty(imgUrls) ? null : StringUtils.split(imgUrls, ',');
            Preconditions.checkArgument(!Strings.isNullOrEmpty(followUpWays), "跟进模式不可以为空值...");
            List<SendChannel> channels = Stream.of(StringUtils.split(followUpWays, ','))
                    .map(x -> SendChannel.paras(Integer.parseInt(x)))
                    .collect(Collectors.toList());
            ts = startTx(request, null);
            getBean(TakeCareService.class, request).batchBirthdayCare(memberIds, channels, followUpContent, arg_imgs, user);
            commitTx(request, ts);
        } catch (Exception e) {
            if (ts != null) rollbackTx(request, ts);
            logger.error("executecare () has error", e);
            throw e;
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/birthday/detail/bymember.json")
    @ResponseBody
    public JsonMessage birthdayCareDetailByMember(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("birthdayCareDetailByMember(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        try {
            loadLoginUser(requestBody, request);
            Integer memberId = MapUtils.getInteger(requestBody, "memberId", 0);
            Preconditions.checkArgument(memberId != 0, "待查看的人员ID不可以为空...");
            Map<String, Object> params = Maps.newHashMap();
            params.put("memberId", memberId);
            params.put("businessType", BusinessType.BIRTHDAYTOUCH.getValue());
            params.put("sql", "birthdayCareDetailByMember");
            Optional<List<Map<String, Object>>> list = getJdbcQuery(request)
                    .queryForList("HisCareRecordEntity", "quert4Details", params);
            return JsonMessageBuilder.OK().withPayload(list).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/ninety/preview/smsconent.json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public JsonMessage previewNinetySmsConent(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("previewNinetySmsConent(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            String taskIds_str = MapUtils.getString(requestBody, "taskIds", null);
            List<Integer> taskIds = Stream.of(StringUtils.split(taskIds_str, ',')).mapToInt(Integer::parseInt).boxed()
                    .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            String cache_key = String.format("PRE_SMS_NINTY_%d", taskIds.hashCode());
            List<CareNinetyTaskAgg> resAggs = null;
            if (getTempCache(request).isPresent()) {
                Object cache_val = getTempCache(request).get().get(cache_key, Object.class);
                if (null != cache_val) resAggs = (List<CareNinetyTaskAgg>) cache_val;
            }
            if (CollectionUtils.isEmpty(resAggs)) {
                resAggs = getBean(TakeCareService.class, request).previewNinetySmsConent(taskIds, null, user);
                if (CollectionUtils.isNotEmpty(resAggs) && getTempCache(request).isPresent()) {
                    getTempCache(request).get().put(cache_key, resAggs);
                }
            }
            if (CollectionUtils.isNotEmpty(resAggs)) {
                List<Map<String, Object>> res = resAggs.stream().map(CareNinetyTaskAgg::toViewMap)
                        .collect(Collectors.toList());
                return JsonMessageBuilder.OK().withPayload(res).toMessage();
            }
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/ninety/rule/reader.json")
    @ResponseBody
    public JsonMessage loadNinetyCareRule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadNinetyCareRule(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            if (user.isStoreManager() || user.isShoppingGuide()) {
                StoEntity store = getBean(StoEntityAction.class, request).loadById(user.getStoreId().orElse(null));
                Optional<CareNinetyRuleEntity> entity = getBean(CareNinetyRuleEntityAction.class, request)
                        .loadByStore(store);
                return JsonMessageBuilder.OK().withPayload(entity.map(CareNinetyRuleEntity::toViewMap).orElse(null)).toMessage();
            } else if (user.isAdmin()) {
                Integer storeId = MapUtils.getInteger(requestBody, "storeId", 0);
                if (storeId != 0) {
                    StoEntity store = getBean(StoEntityAction.class, request).loadById(storeId);
                    Optional<CareNinetyRuleEntity> entity = getBean(CareNinetyRuleEntityAction.class, request)
                            .loadByStore(store);
                    return JsonMessageBuilder.OK().withPayload(entity.map(CareNinetyRuleEntity::toViewMap).orElse(null)).toMessage();
                }
                OrgEntity company = getBean(OrgEntityAction.class, request).loadComById(user.getCompanyId());
                Optional<CareNinetyRuleEntity> entity = getBean(CareNinetyRuleEntityAction.class, request)
                        .loadByCompany(company);
                return JsonMessageBuilder.OK().withPayload(entity.map(CareNinetyRuleEntity::toViewMap).orElse(null)).toMessage();
            }
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    // int toHour, int toNode1,
// int toNode3, int toNode7, int toNode15, int toNode30, int toNode60, int toNode90,
// String remark, int limitDays, double minAmount, double limitAmount
    @RequestMapping(value = "/ninety/rule/save.json")
    @ResponseBody
    public JsonMessage saveNinetyCareRule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("saveNinetyCareRule(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            int toHour = MapUtils.getIntValue(requestBody, "toHour", 0);
            int toHourDelay = MapUtils.getIntValue(requestBody, "toHourDelay", 0);
            int toNode1 = MapUtils.getIntValue(requestBody, "toNode1", 0);
            int toNode1Delay = MapUtils.getIntValue(requestBody, "toNode1Delay", 1);
            int toNode3 = MapUtils.getIntValue(requestBody, "toNode3", 0);
            int toNode3Delay = MapUtils.getIntValue(requestBody, "toNode3Delay", 1);
            int toNode7 = MapUtils.getIntValue(requestBody, "toNode7", 0);
            int toNode7Delay = MapUtils.getIntValue(requestBody, "toNode7Delay", 0);
            int toNode15 = MapUtils.getIntValue(requestBody, "toNode15", 0);
            int toNode15Delay = MapUtils.getIntValue(requestBody, "toNode15Delay", 0);
            int toNode30 = MapUtils.getIntValue(requestBody, "toNode30", 0);
            int toNode30Delay = MapUtils.getIntValue(requestBody, "toNode30Delay", 0);
            int toNode60 = MapUtils.getIntValue(requestBody, "toNode60", 0);
            int toNode60Delay = MapUtils.getIntValue(requestBody, "toNode60Delay", 0);
            int toNode90 = MapUtils.getIntValue(requestBody, "toNode90", 0);
            int toNode90Delay = MapUtils.getIntValue(requestBody, "toNode90Delay", 0);
            int limitDays = MapUtils.getIntValue(requestBody, "limitDays", 0);
            double minAmount = MapUtils.getDoubleValue(requestBody, "minAmount", 0.00D);
            double limitAmount = MapUtils.getDoubleValue(requestBody, "limitAmount", 0.00D);
            double mergeAmount = MapUtils.getDoubleValue(requestBody, "mergeAmount", 0.00D);
            String remark = MapUtils.getString(requestBody, "remark", null);
            boolean appNext = MapUtils.getIntValue(requestBody, "appNext", 0) == 1;
            if (user.isStoreManager() || user.isShoppingGuide()) {
                StoEntity store = getBean(StoEntityAction.class, request).loadById(user.getStoreId().orElse(null));
                getBean(CareNinetyRuleEntityAction.class, request).saveByStore(store, toHour, toNode1,
                        toNode3, toNode7, toNode15, toNode30, toNode60, toNode90,
                        remark, limitDays, minAmount, limitAmount, toHourDelay, toNode1Delay, toNode3Delay, toNode7Delay,
                        toNode15Delay, toNode30Delay, toNode60Delay, toNode90Delay, mergeAmount);
            } else {
                String storeIds_str = MapUtils.getString(requestBody, "storeIds", null);
                if (Strings.isNullOrEmpty(storeIds_str)) {
                    OrgEntity company = getBean(OrgEntityAction.class, request).loadComById(user.getCompanyId());
                    getBean(CareNinetyRuleEntityAction.class, request).saveByCompany(company, toHour, toNode1,
                            toNode3, toNode7, toNode15, toNode30, toNode60, toNode90,
                            remark, limitDays, minAmount, limitAmount, toHourDelay, toNode1Delay, toNode3Delay, toNode7Delay,
                            toNode15Delay, toNode30Delay, toNode60Delay, toNode90Delay, mergeAmount, appNext);
                } else {
                    List<Integer> storeIds = Stream.of(StringUtils.split(storeIds_str, ',')).mapToInt(Integer::parseInt)
                            .boxed().collect(Collectors.toList());
                    Optional<List<StoEntity>> stores_opt = getBean(StoEntityAction.class, request).findByIds(storeIds);
                    stores_opt.ifPresent(x -> getBean(CareNinetyRuleEntityAction.class, request).saveByStores(x, toHour, toNode1,
                            toNode3, toNode7, toNode15, toNode30, toNode60, toNode90,
                            remark, limitDays, minAmount, limitAmount, toHourDelay, toNode1Delay, toNode3Delay, toNode7Delay,
                            toNode15Delay, toNode30Delay, toNode60Delay, toNode90Delay, mergeAmount));
                }
            }
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }


    @RequestMapping(value = "/ninety/single/carelog.json")
    @ResponseBody
    public JsonMessage ninetySinleCareLog(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("ninetySinleCareLog(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        try {
            loadLoginUser(requestBody, request);
            Integer planId = MapUtils.getInteger(requestBody, "planId", null);
            Preconditions.checkArgument(null != planId, "非法的 planId 取值...");
            Map<String, Object> params = Maps.newHashMap();
            params.put("planId", planId);
            Optional<List<Map<String, Object>>> list = getJdbcQuery(request)
                    .queryForList("CareNinetyEntity", "quertDetailByNinety", params);
            return JsonMessageBuilder.OK().withPayload(list).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/ninety/executecare.json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public JsonMessage executeNinetyCare(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("executeNinetyCare(requestBody=%s,url=%s) start", requestBody,
                    request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        String cache_key = null;
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            String taskIds_str = MapUtils.getString(requestBody, "taskIds", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(taskIds_str), "待发送的任务不可以为空...");
            List<Integer> taskIds = Stream.of(StringUtils.split(taskIds_str, ',')).mapToInt(Integer::parseInt)
                    .boxed().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            cache_key = String.format("PRE_SMS_NINTY_%d", taskIds.hashCode());
            List<CareNinetyTaskAgg> resAggs = null;
            if (getTempCache(request).isPresent()) {
                Object cache_val = getTempCache(request).get().get(cache_key, Object.class);
                if (null != cache_val) resAggs = (List<CareNinetyTaskAgg>) cache_val;
            }
            String followUpContent = MapUtils.getString(requestBody, "followUpContent", null);
            String followUpWays = MapUtils.getString(requestBody, "followUpWays", null);
            String imgUrls = MapUtils.getString(requestBody, "imgUrls", null);
            String[] arg_imgs = Strings.isNullOrEmpty(imgUrls) ? null : StringUtils.split(imgUrls, ',');
            Preconditions.checkArgument(!Strings.isNullOrEmpty(followUpWays), "跟进模式不可以为空值...");
            List<SendChannel> channels = Stream.of(StringUtils.split(followUpWays, ','))
                    .map(x -> SendChannel.paras(Integer.parseInt(x)))
                    .collect(Collectors.toList());
            getBean(TakeCareService.class, request).batchNinetyCare(taskIds, channels, followUpContent, arg_imgs, resAggs, user);
        } finally {
            LoginContextHolder.clear();
            if (getTempCache(request).isPresent() && !Strings.isNullOrEmpty(cache_key)) {
                getTempCache(request).get().evict(cache_key);
            }
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    private UserAuthorEntity loadLoginUser(Map<String, Object> requestBody, HttpServletRequest request) {
        Integer userId = MapUtils.getInteger(requestBody, "userId", 0);
        Preconditions.checkArgument(userId != 0, "登陆用户userId值非法...");
        return getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
    }

    private JdbcQuerySupport getJdbcQuery(HttpServletRequest request) {
        return getBean("takeCareJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

    private Optional<Cache> getTempCache(HttpServletRequest request) {
        Cache cache = getBean("takeCareCacheManager", CaffeineCacheManager.class, request).getCache("tempCareCache");
        return Optional.ofNullable(cache);
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", PlatformTransactionManager.class, request);
    }
}
