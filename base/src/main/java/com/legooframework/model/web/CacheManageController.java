package com.legooframework.model.web;

import com.legooframework.model.cache.CaffeineCacheManager;
import com.legooframework.model.base.runtime.LoginContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@RestController(value = "CacheManageController")
@RequestMapping(value = "/cache")
public class CacheManageController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CacheManageController.class);

    @RequestMapping(value = "/names.json")
    public JsonMessage getCacheList(HttpServletRequest request) {
        LoginContextHolder.get();
        Collection<String> cache_names = getBean(CaffeineCacheManager.class, request).getCacheNames();
        return JsonMessageBuilder.OK().withPayload(cache_names).toMessage();
    }

    @RequestMapping(value = "/{cacheName}/clear.json")
    public JsonMessage clearCache(@PathVariable String cacheName, HttpServletRequest request) {
        LoginContextHolder.get();
        if (getBean(CaffeineCacheManager.class, request).containsCache(cacheName)) {
            Cache cache = getBean(CaffeineCacheManager.class, request).getCache(cacheName);
            if (cache != null)
                cache.clear();
            if (logger.isDebugEnabled())
                logger.debug(String.format("Cache Which Name is %s has been clear success...", cacheName));
            return JsonMessageBuilder.OK().toMessage();
        }
        return JsonMessageBuilder.WARN(String.format("不存在cacheName=%s 对应的Cache实例.", cacheName))
                .toMessage();
    }

}
