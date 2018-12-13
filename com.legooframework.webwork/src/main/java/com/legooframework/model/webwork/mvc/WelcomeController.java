package com.legooframework.model.webwork.mvc;

import com.google.common.collect.Maps;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.webwork.entity.LoginTokenAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController(value = "welcomeController")
@RequestMapping(value = "/online")
public class WelcomeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

    @RequestMapping(value = "/users/amount.json")
    public JsonMessage getOnlineUserAmount(HttpServletRequest request) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("onlineUserAmount", getBean(LoginTokenAction.class, request).totalOnlineNum());
        return JsonMessageBuilder.OK().withPayload(map).toMessage();
    }

    @RequestMapping(value = "/{cacheName}/clear.json")
    public JsonMessage clearCache(@PathVariable String cacheName, HttpServletRequest request) {
        CaffeineCacheManager cacheManager = getBean(CaffeineCacheManager.class, request);
        if (StringUtils.equals("all", cacheName)) {
            cacheManager.clearAll();
        } else {
            cacheManager.clearByCache(cacheName);
        }
        return JsonMessageBuilder.OK().toMessage();
    }

}
