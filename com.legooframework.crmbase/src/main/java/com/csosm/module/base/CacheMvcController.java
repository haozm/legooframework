package com.csosm.module.base;

import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.cache.GuavaCache;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Controller(value = "cacheController")
@RequestMapping("/cache")
public class CacheMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CacheMvcController.class);

    /**
     * 清空默认名称的cache
     *
     * @return null
     */
    @RequestMapping(value = "/clear/defcache.json")
    @ResponseBody
    public Map<String, Object> clearDefaultCache() {
        baseAdapterServer.cleanCache("adapterCache");
        return wrapperEmptyResponse();
    }

    /**
     * 清空指定名称的cache
     *
     * @return null
     */
    @RequestMapping(value = "/clear/byname.json")
    @ResponseBody
    public Map<String, Object> clearCacheAction(@RequestBody Map<String, String> requestBody) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("clearCacheAction(requestBody=%s)", requestBody));
        String cache_name = MapUtils.getString(requestBody, "cacheName");
        if (!Strings.isNullOrEmpty(cache_name))
            baseAdapterServer.cleanCache(cache_name);
        return wrapperEmptyResponse();
    }

    // 加载当前配置运行的cache list
    @RequestMapping(value = "/load/all.json")
    @ResponseBody
    public Map<String, Object> loadCacheListAction() {
        Collection<GuavaCache> caches = baseAdapterServer.getAllCaches();
        List<Map<String, String>> list = Lists.newArrayList();
        for (GuavaCache cache : caches) {
            Map<String, String> desc = Maps.newHashMap();
            desc.put("name", cache.getName());
            switch (cache.getName()) {
                case "adapterCache":
                    desc.put("desc", "门店、导购、组织树相关信息Cache");
                    break;
                case "urlCache":
                    desc.put("desc", "短连接相关数据Cache");
                    break;
                case "defCache":
                    desc.put("desc", "静态数据资源Cache");
                    break;
                case "couponsCache":
                    desc.put("desc", "优惠券相关数据资源Cache");
                    break;
                case "tempCache":
                    desc.put("desc", "优惠券相关数据资源Cache");
                    break;
                default:
                    desc.put("desc", "尚未标识的Cache");
                    break;
            }
            list.add(desc);
        }
        return wrapperResponse(list);
    }

    @Resource
    private BaseModelServer baseAdapterServer;

}
