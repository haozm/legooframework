package com.legooframework.model.redis.entity;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Map;

public class RedisCacheManagerFactoryBean extends AbstractFactoryBean<RedisCacheManager> {

    private final static String DEFAULTCONFIG_VALUE = "ttl=0";
    private RedisSerializationContext.SerializationPair<String> serializationPair =
            RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string());
    private RedisConnectionFactory redisConnectionFactory;

    public RedisCacheManagerFactoryBean(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Class<RedisCacheManager> getObjectType() {
        return RedisCacheManager.class;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected RedisCacheManager createInstance() throws Exception {
        RedisCacheConfiguration defcacheCfg = buildCacheConfig(Strings.isNullOrEmpty(defaultConfig) ?
                DEFAULTCONFIG_VALUE : defaultConfig);
        if (logger.isDebugEnabled())
            logger.debug(String.format("[Cache]Builder cacheDefaults Config is: %s", Strings.isNullOrEmpty(defaultConfig) ?
                    DEFAULTCONFIG_VALUE : defaultConfig));
        Map<String, RedisCacheConfiguration> cacheConfigurationMap = Maps.newHashMap();
        if (MapUtils.isNotEmpty(cacheConfigs)) {
            for (Map.Entry<String, String> entry : cacheConfigs.entrySet()) {
                String _cacheName = entry.getKey();
                RedisCacheConfiguration _cacheConfig = buildCacheConfig(entry.getValue());
                if (logger.isDebugEnabled())
                    logger.debug(String.format("[Cache]Builder cacheConfig is: [%s,%s]", _cacheName, entry.getValue()));
                cacheConfigurationMap.put(_cacheName, _cacheConfig);
            }
        }
        if (MapUtils.isNotEmpty(cacheConfigurationMap)) {
            return RedisCacheManager.RedisCacheManagerBuilder
                    .fromConnectionFactory(redisConnectionFactory)
                    .cacheDefaults(defcacheCfg).withInitialCacheConfigurations(cacheConfigurationMap)
                    .transactionAware().build();
        }
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(defcacheCfg).transactionAware().build();
    }

    private String defaultConfig;
    private Map<String, String> cacheConfigs;

    /**
     * 按照命名空间来创建 CACHE_NAME OOXOX
     * ttl=0,enabledNull=false,prefix=null
     *
     * @param nameSpace nameSpace
     * @return RedisCacheConfiguration
     */
    private RedisCacheConfiguration buildCacheConfig(String nameSpace) {
        Map<String, String> values = Splitter.on(',').trimResults().withKeyValueSeparator('=').split(nameSpace);
        long ttl = MapUtils.getLongValue(values, "ttl", 0);
        boolean enabledNull = MapUtils.getBooleanValue(values, "enabledNull", false);
        String preifx = MapUtils.getString(values, "prefix", null);
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl == 0L ? Duration.ZERO : Duration.ofSeconds(ttl))
                .serializeKeysWith(serializationPair);
        if (!enabledNull) cacheConfig.disableCachingNullValues();
        if (Strings.isNullOrEmpty(preifx)) {
            cacheConfig.disableKeyPrefix();
        } else {
            cacheConfig.prefixKeysWith(preifx);
        }
        return cacheConfig;
    }

}
