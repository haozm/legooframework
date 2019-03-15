package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.config.FileMonitorEvent;
import com.legooframework.model.core.config.FileReloadSupport;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TenantsRouteFactory extends FileReloadSupport<File> {

    private static final Logger logger = LoggerFactory.getLogger(TenantsRouteFactory.class);
    private RulesModule rulesModule;
    private final List<TenantsRouteEntity> tenantsRoutes;

    TenantsRouteFactory(RulesModule rulesModule, List<String> patterns) {
        super(patterns);
        this.rulesModule = rulesModule;
        this.tenantsRoutes = Lists.newArrayList();
    }

    public String getUrl(Integer companyId, String name) {
        Optional<TenantsRouteEntity> tenants = this.tenantsRoutes.stream().filter(x -> x.contains(companyId)).findFirst();
        Preconditions.checkState(tenants.isPresent(), "当前无ID=%s 的公司配置信息...", companyId);
        String url = tenants.get().getUrl(name);
        if (logger.isDebugEnabled())
            logger.debug(String.format("getUrl(%s,%s) res %s", companyId, name, url));
        return url;
    }

    /**
     * 网络请求 解析返回值
     *
     * @param postUrl
     * @return
     */
    public Optional<JsonElement> post(String postUrl, Map<String, Object> params, Object... pathVariables) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,params=%s", postUrl, params));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        _params.put("bundle", "com.legooframework.crmadapter");
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(postUrl, pathVariables)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .syncBody(_params)
                .retrieve().bodyToMono(String.class);
        String payload = mono.block();
        Preconditions.checkNotNull(payload, "数据无返回，通信异常...");
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,params=%s,return %s", postUrl, _params, payload.length()));
        return WebUtils.parseJson(payload);
    }

    @Override
    public void handleileMonitorEvent(FileMonitorEvent monitorEvent) {
        // TODO
    }

    void build(File file) {
        if (!isSupported(file)) return;
        boolean error = false;
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        List<TenantsRouteEntity> _tenantsRoutes = Lists.newArrayList();
        try {
            digester.push(_tenantsRoutes);
            digester.parse(file);
            if (logger.isDebugEnabled()) logger.debug(String.format("finish parse sql-cfg: %s", file));
            super.addConfig(file, file);
        } catch (Exception e) {
            logger.error(String.format("parse file=%s has error", file), e);
            error = true;
        } finally {
            digester.clear();
        }
        if (!error) {
            this.tenantsRoutes.clear();
            this.tenantsRoutes.addAll(_tenantsRoutes);
        }
    }
}
