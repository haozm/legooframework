package com.legooframework.model.httpproxy.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.legooframework.model.core.config.FileReloadSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class HttpGateWayFactory extends FileReloadSupport<File> {

    private static final Logger logger = LoggerFactory.getLogger(HttpGateWayFactory.class);

    private final RulesModule rulesModule;
    private final List<HttpGateWayEntity> gateWays;
    private final Cache<String, FusingCountEntity> fusingCache;
    private final HashMultimap<String, String> multimap = HashMultimap.create();
    private final Timer timer = new Timer();

    HttpGateWayFactory(List<String> patterns, RulesModule rulesModule, FusingCountEntityAction fusingCountAction) {
        super(patterns);
        this.rulesModule = rulesModule;
        this.gateWays = Lists.newArrayList();
        CacheRemovalListener removalListener = new CacheRemovalListener(fusingCountAction);
        this.fusingCache = CacheBuilder.from("initialCapacity=512,maximumSize=4096,expireAfterAccess=3m")
                .removalListener(removalListener).build();
        timer.scheduleAtFixedRate(new CacheTimerTask(), 0L, 1000 * 60L);
    }

    void destroy() {
        this.timer.cancel();
        this.multimap.clear();
        this.fusingCache.invalidateAll();
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
        LocalDateTime now = LocalDateTime.now();
        String key_key = now.toString("yyyyMMddHHmm00");
        String cache_key = fusingCount.getCacheKey();
        FusingCountEntity cache_value = fusingCache.getIfPresent(cache_key);
        this.multimap.put(key_key, cache_key);
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

    private class CacheTimerTask extends TimerTask {

        CacheTimerTask() {
        }

        @Override
        public void run() {
            LocalDateTime before_minutes = LocalDateTime.now().plusMinutes(-1);
            LocalDateTime before_two_minutes = LocalDateTime.now().plusMinutes(-2);
            String key = before_minutes.toString("yyyyMMddHHmm00");
            removeAll(multimap.removeAll(key));
            key = before_two_minutes.toString("yyyyMMddHHmm00");
            removeAll(multimap.removeAll(key));
        }

        private void removeAll(Set<String> cacheKeys) {
            if (CollectionUtils.isEmpty(cacheKeys)) return;
            fusingCache.invalidateAll(cacheKeys);
            if (logger.isDebugEnabled())
                logger.debug(String.format("fusingCache.invalidateAll(%s)", cacheKeys));
        }
    }
}
