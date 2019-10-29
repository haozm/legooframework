package com.legooframework.model.takecare.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.BusinessType;
import com.legooframework.model.covariant.entity.SendChannel;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
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

    @RequestMapping(value = "/birthday/executecare.json")
    @ResponseBody
    public JsonMessage executeBirthdayCare(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("executeBirthdayCare(requestBody=%s,url=%s) start", requestBody, request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        TransactionStatus ts = null;
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            String mIds = MapUtils.getString(requestBody, "memberIds", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(mIds), "待发送的人员ID不可以为空...");
            List<Integer> memberIds = Stream.of(StringUtils.split(mIds, ',')).mapToInt(Integer::parseInt)
                    .boxed().collect(Collectors.toList());
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
            logger.debug(String.format("birthdayCareDetailByMember(requestBody=%s,url=%s) start", requestBody, request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        try {
            loadLoginUser(requestBody, request);
            Integer memberId = MapUtils.getInteger(requestBody, "memberId", 0);
            Preconditions.checkArgument(memberId != 0, "待查看的人员ID不可以为空...");
            Map<String, Object> params = Maps.newHashMap();
            params.put("memberId", memberId);
            params.put("businessType", BusinessType.BIRTHDAYCARE.getValue());
            params.put("sql", "birthdayCareDetailByMember");
            Optional<List<Map<String, Object>>> list = getJdbcQuery(request).queryForList("HisCareRecordEntity", "quert4Details", params);
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
            logger.debug(String.format("previewNinetySmsConent(requestBody=%s,url=%s) start", requestBody, request.getRequestURL().toString()));
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

    @RequestMapping(value = "/ninety/single/carelog.json")
    @ResponseBody
    public JsonMessage ninetySinleCareLog(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("ninetySinleCareLog(requestBody=%s,url=%s) start", requestBody, request.getRequestURL().toString()));
        LoginContextHolder.setAnonymousCtx();
        try {
            loadLoginUser(requestBody, request);
            Integer planId = MapUtils.getInteger(requestBody, "planId", null);
            Preconditions.checkArgument(null != planId, "非法的 planId 取值...");
            Map<String, Object> params = Maps.newHashMap();
            params.put("planId", planId);
            Optional<List<Map<String, Object>>> list = getJdbcQuery(request).queryForList("CareNinetyEntity", "quertDetailByNinety", params);
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
            logger.debug(String.format("executeNinetyCare(requestBody=%s,url=%s) start", requestBody, request.getRequestURL().toString()));
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
