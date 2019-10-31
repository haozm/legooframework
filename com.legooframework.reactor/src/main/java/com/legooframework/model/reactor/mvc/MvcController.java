package com.legooframework.model.reactor.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.reactor.entity.ReactorSwitchEntity;
import com.legooframework.model.reactor.entity.ReactorSwitchEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/reactor")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("reactorBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    @RequestMapping(value = "/cache/clean.json")
    @ResponseBody
    public JsonMessage cacheClean(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("cacheClean(url=%s)", request.getRequestURI()));
        LoginContextHolder.setAnonymousCtx();
        String cacheName = request.getParameter("cache");
        if (Strings.isNullOrEmpty(cacheName)) cacheName = "reactor";
        CaffeineCacheManager cacheManager = null;
        if (StringUtils.equals("reactor", cacheName)) {
            cacheManager = getBean("reactorCacheManager", CaffeineCacheManager.class, request);
            cacheManager.clearByCache("reactorCache");
        } else if (StringUtils.equals("covariant", cacheName)) {
            cacheManager = getBean("covariantCacheManager", CaffeineCacheManager.class, request);
            cacheManager.clearByCache("covariantCache");
        } else if (StringUtils.equals("all", cacheName)) {
            cacheManager = getBean("covariantCacheManager", CaffeineCacheManager.class, request);
            cacheManager.clearByCache("covariantCache");
            cacheManager = getBean("reactorCacheManager", CaffeineCacheManager.class, request);
            cacheManager.clearByCache("reactorCache");
        } else {
            logger.warn(String.format("无法匹配核实的cache=%s", cacheName));
        }
        LoginContextHolder.clear();
        return JsonMessageBuilder.OK().toMessage();
    }


    @RequestMapping(value = "/setting/{type}/switched.json")
    @ResponseBody
    public JsonMessage setReactorSwitched(@PathVariable(value = "type") String type,
                                          @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("setReactorSwitched(url=%s,requestBody=%s)", request.getRequestURI(), requestBody));
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        if (StringUtils.equals(ReactorSwitchEntity.TYPE_RETAILFACT, type)) {
            OrgEntity company = getBean(OrgEntityAction.class, request).loadComById(user.getCompanyId());
            String store_ids = MapUtils.getString(requestBody, "storeIds", null);
            Optional<List<StoEntity>> stores = Optional.empty();
            if (!Strings.isNullOrEmpty(store_ids)) {
                Set<Integer> storeIds = Stream.of(StringUtils.split(store_ids, ',')).mapToInt(Integer::parseInt).boxed()
                        .collect(Collectors.toSet());
                stores = getBean(StoEntityAction.class, request).findByIds(storeIds);
            }
            getBean(ReactorSwitchEntityAction.class, request).eidtRetailFactSwitch(company, stores.orElse(null));
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/read/{type}/switched.json")
    @ResponseBody
    public JsonMessage readReactorSwitched(@PathVariable(value = "type") String type,
                                           @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("readReactorSwitched(url=%s,requestBody=%s)", request.getRequestURI(), requestBody));
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        if (StringUtils.equals(ReactorSwitchEntity.TYPE_RETAILFACT, type)) {
            OrgEntity company = getBean(OrgEntityAction.class, request).loadComById(user.getCompanyId());
            Optional<ReactorSwitchEntity> optional = getBean(ReactorSwitchEntityAction.class, request)
                    .findRetailFactSwitch(company);
            return JsonMessageBuilder.OK().withPayload(optional.map(ReactorSwitchEntity::toViewMap).orElse(null)).toMessage();
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    private UserAuthorEntity loadLoginUser(Map<String, Object> requestBody, HttpServletRequest request) {
        Integer userId = MapUtils.getInteger(requestBody, "userId", 0);
        Preconditions.checkArgument(userId != 0, "登陆用户userId值非法...");
        return getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
    }

}
