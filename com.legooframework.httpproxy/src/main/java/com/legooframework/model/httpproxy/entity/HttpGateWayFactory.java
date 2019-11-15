package com.legooframework.model.httpproxy.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.legooframework.model.core.config.FileReloadSupport;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class HttpGateWayFactory extends FileReloadSupport<File> {

    private static final Logger logger = LoggerFactory.getLogger(HttpGateWayFactory.class);

    private final RulesModule rulesModule;
    private final List<HttpGateWayEntity> gateWays;
    private final Cache<String, FusingCountEntity> fusingCache;

    HttpGateWayFactory(List<String> patterns, RulesModule rulesModule, FusingCountEntityAction fusingCountAction) {
        super(patterns);
        this.rulesModule = rulesModule;
        this.gateWays = Lists.newArrayList();
        CacheRemovalListener removalListener = new CacheRemovalListener(fusingCountAction);
        this.fusingCache = CacheBuilder.from("initialCapacity=512,maximumSize=4096,expireAfterAccess=3m")
                .removalListener(removalListener).build();
    }

    public String getTarget(HttpRequestDto requestDto) {
        String target = null;
        HttpGateWayEntity gateWay = null;
        for (HttpGateWayEntity $it : gateWays) {
            if ($it.match(requestDto.getUriComponents())) {
                gateWay = $it;
                target = $it.getTatget(requestDto.getUriComponents());
                break;
            }
        }
        Preconditions.checkState(!Strings.isNullOrEmpty(target), "Uri=%s 无匹配数据...", requestDto.getUri());
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s] matched [%s]", requestDto.getUri(), target));
        pushCache(new FusingCountEntity(gateWay, requestDto));
        return target;
    }

    /**
     * @param fusingCount OOXX
     */
    private void pushCache(FusingCountEntity fusingCount) {
        String cache_key = fusingCount.getCacheKey();
        FusingCountEntity cache_value = fusingCache.getIfPresent(cache_key);
        if (null == cache_value) {
            fusingCache.put(fusingCount.getCacheKey(), fusingCount);
        } else {
            cache_value.increment();
        }
    }


    @Override
    public void addConfig(File file, File config) {
        super.addConfig(file, config);
    }

    @Override
    protected Optional<File> parseFile(File file) {
        if (!isSupported(file)) return Optional.empty();
        Digester digester = DigesterLoader.newLoader(this.rulesModule).newDigester();
        List<HttpGateWayEntity> items = Lists.newArrayList();
        try {
            digester.push(items);
            digester.parse(file);
            if (logger.isDebugEnabled())
                logger.debug(String.format("finish parse getway-rule: %s", items));
            this.gateWays.addAll(items);
            return Optional.of(file);
        } catch (Exception e) {
            logger.error(String.format("parse file=%s has error", file), e);
        } finally {
            digester.clear();
        }
        return Optional.empty();
    }


    private static class CacheRemovalListener implements RemovalListener<String, FusingCountEntity> {

        CacheRemovalListener(FusingCountEntityAction fusingCountAction) {
            this.fusingCountAction = fusingCountAction;
        }

        private FusingCountEntityAction fusingCountAction;

        @Override
        public void onRemoval(RemovalNotification<String, FusingCountEntity> removal) {
            if (fusingCountAction != null) {
                fusingCountAction.insert(removal.getValue());
            }
            if (logger.isDebugEnabled())
                logger.debug(String.format("RemovalListener %s Removaled", removal.getValue()));
        }
    }
}
