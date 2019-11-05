package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.config.FileReloadSupport;
import com.legooframework.model.core.utils.WebUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TenantsRouteFactory extends FileReloadSupport<File> {

    private static final Logger logger = LoggerFactory.getLogger(TenantsRouteFactory.class);
    private RulesModule rulesModule;
    private RestTemplate restTemplate;

    private final List<UrlItem> urlItems = Lists.newArrayList();
    private final List<TenantsDomainEntity> domainList = Lists.newArrayList();

    TenantsRouteFactory(RulesModule rulesModule, RestTemplate restTemplate, List<String> patterns) {
        super(patterns);
        this.rulesModule = rulesModule;
        this.restTemplate = restTemplate;
    }

    public String getUrl(Integer companyId, String name) {
        Optional<UrlItem> exits = this.urlItems.stream().filter(x -> StringUtils.equals(x.getName(), name)).findFirst();
        Preconditions.checkState(exits.isPresent(), "不存在 %s 对应的目标地址....", name);
        if (companyId == null || companyId == -1) {
            Optional<TenantsDomainEntity> domain_opt = this.domainList.stream().filter(TenantsDomainEntity::isDefaulted).findFirst();
            Preconditions.checkState(domain_opt.isPresent(), "缺失默认Domain 配置....");
            boolean has_fragment = domain_opt.get().hasFragment(name);
            String url = exits.get().toUrl(domain_opt.get().getDomain(), has_fragment);
            if (logger.isTraceEnabled())
                logger.trace(String.format("getUrl(null,%s) res %s", name, url));
            return url;
        }

        Optional<TenantsDomainEntity> domain_opt = this.domainList.stream().filter(x -> x.contains(companyId)).findFirst();
        boolean has_fragment = true;
        String url = null;
        if (domain_opt.isPresent()) {
            has_fragment = domain_opt.get().hasFragment(name);
            url = exits.get().toUrl(domain_opt.get().getDomain(), has_fragment);
        } else {
            url = exits.get().toUrl(null, true);
        }
        if (logger.isTraceEnabled())
            logger.trace(String.format("getUrl(%s,%s) res %s", companyId, name, url));
        Preconditions.checkState(StringUtils.startsWith(url, "http"), "非合法的URL %s", url);
        return url;
    }

    /**
     * 网络请求 解析返回值
     *
     * @param postUrl 请求地址
     * @return 我的太阳
     */
    Optional<JsonElement> post(String postUrl, String token, Map<String, Object> params, Object... pathVariables) {
        if (logger.isTraceEnabled())
            logger.trace(String.format("URL=%s,params,size =%s pathVariables=%s,token=%s", postUrl,
                    MapUtils.isEmpty(params) ? 0 : params.size(), Arrays.toString(pathVariables), token));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        _params.put("bundle", "com.legooframework.crmadapter");
        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(postUrl, pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .header(BaseEntityAction.KEY_HEADER, token)
                .bodyValue(_params)
                .retrieve().bodyToMono(String.class);
        String payload = mono.block();
        Preconditions.checkNotNull(payload, "数据无返回，通信异常...");
        if (logger.isTraceEnabled())
            logger.trace(String.format("URL=%s,params=%s,return %s", postUrl, MapUtils.isEmpty(params) ? 0 : params.size(),
                    payload.length()));
        return WebUtils.parseJson(payload);
    }

    Optional<JsonElement> postWithRest(String postUrl, Map<String, Object> params, Object... pathVariables) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,params=%s pathVariables=%s", postUrl, MapUtils.isEmpty(params) ? 0 : params.size(),
                    Arrays.toString(pathVariables)));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        _params.put("bundle", "com.legooframework.crmadapter");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(_params, headers);
        ResponseEntity<String> resp = this.restTemplate.exchange(postUrl, HttpMethod.POST, requestEntity, String.class, pathVariables);
        Preconditions.checkNotNull(resp.getBody(), "数据无返回，通信异常...");
        if (logger.isTraceEnabled())
            logger.trace(String.format("URL=%s,params=%s,return %s", postUrl, MapUtils.isEmpty(params) ? 0 : params.size(),
                    resp.getBody().length()));
        return WebUtils.parseJson(resp.getBody());
    }

    /**
     * 网络请求 解析返回值
     *
     * @param postUrl 情深依旧
     * @return json
     */
    public Optional<JsonElement> post(String postUrl, Map<String, Object> params, Object... pathVariables) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,params=%s pathVariables=%s", postUrl, MapUtils.isEmpty(params) ? 0 : params.size(),
                    Arrays.toString(pathVariables)));
        Map<String, Object> _params = Maps.newHashMap();
        if (MapUtils.isNotEmpty(params)) _params.putAll(params);
        _params.put("bundle", "com.legooframework.crmadapter");

        Mono<String> mono = WebClient.create().method(HttpMethod.POST)
                .uri(postUrl, pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(_params)
                .retrieve().bodyToMono(String.class);
        String payload = mono.block(Duration.ofSeconds(10));
        Preconditions.checkNotNull(payload, "%s数据无返回，通信异常...", postUrl);
        if (logger.isTraceEnabled())
            logger.trace(String.format("URL=%s,params=%s,return %s", postUrl, MapUtils.isEmpty(params) ? 0 : params.size(),
                    payload.length()));
        return WebUtils.parseJson(payload);
    }

    protected Optional<File> parseFile(File file) {
        if (!isSupported(file)) return Optional.empty();
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        TenantsRouteFactoryBuilder factoryBuilder = new TenantsRouteFactoryBuilder();
        try {
            digester.push(factoryBuilder);
            digester.parse(file);
            if (logger.isDebugEnabled()) logger.debug(String.format("finish parse sql-cfg: %s", file));
        } catch (Exception e) {
            logger.error(String.format("parse file=%s has error", file), e);
            return Optional.empty();
        } finally {
            digester.clear();
        }
        this.urlItems.clear();
        this.urlItems.addAll(factoryBuilder.getUrlItems());
        this.domainList.clear();
        this.domainList.addAll(factoryBuilder.getDomains());
        return Optional.of(file);
    }

    @Override
    protected void addConfig(File file, File config) {
        super.addConfig(file, config);
    }

    public static UrlItem createUrlItem(String name, String fragment, String url) {
        return new UrlItem(name, fragment, url);
    }

    public static class UrlItem {

        private final String name, fragment, url;

        UrlItem(String name, String fragment, String url) {
            this.name = name;
            this.fragment = fragment;
            this.url = url;
        }

        String toUrl(String prefix, boolean hasFragment) {
            if (StringUtils.startsWith(this.url, "http")) return this.url;
            if (hasFragment && StringUtils.isNotEmpty(this.fragment))
                return String.format("%s/%s/%s", prefix, fragment, url);
            return String.format("%s/%s", prefix, url);
        }

        String getName() {
            return name;
        }

        String getUrl() {
            return url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UrlItem urlItem = (UrlItem) o;
            return com.google.common.base.Objects.equal(name, urlItem.name);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(name);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .add("fragment", fragment)
                    .add("url", url)
                    .toString();
        }
    }
}
