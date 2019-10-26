package com.legooframework.model.reactor.mvc;

import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/manager")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

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

}
