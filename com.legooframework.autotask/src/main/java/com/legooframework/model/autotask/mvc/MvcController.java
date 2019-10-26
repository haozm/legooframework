package com.legooframework.model.autotask.mvc;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.autotask.entity.Constant;
import com.legooframework.model.autotask.entity.DelayType;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController(value = "autotaskMvcController")
@RequestMapping(value = "/autotask")
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

    @RequestMapping(value = "/enum/{enumType}/list.json")
    @ResponseBody
    public JsonMessage enumTypeList(@PathVariable(value = "enumType") String enumType, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("enumTypeList(url=%s)", request.getRequestURI()));
        List<Map<String, Object>> mapList = Lists.newArrayList();
        if (StringUtils.equals("DelayType", enumType)) {
            Stream.of(DelayType.values()).forEach(bt -> {
                Map<String, Object> param = Maps.newHashMap();
                param.put("value", bt.getValue());
                param.put("desc", bt.getDesc());
                mapList.add(param);
            });
        } else {
            throw new IllegalArgumentException(String.format("非法的入参 enumType = %s", enumType));
        }
        return JsonMessageBuilder.OK().withPayload(mapList).toMessage();
    }

}
