package com.legooframework.model.redis.entity;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class RedisCacheManagerFactoryBean extends AbstractFactoryBean<RedisCacheManager> {

    private final static String DEFAULTCONFIG_VALUE = "name=cacheName,ttl=600,nullValue=false,prefix=null";
    private RedisSerializer<String> valueSerialization = RedisSerializer.string();
    private RedisConnectionFactory redisConnectionFactory;

    public RedisCacheManagerFactoryBean(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Class<RedisCacheManager> getObjectType() {
        return RedisCacheManager.class;
    }

    @Override
    protected RedisCacheManager createInstance() throws Exception {
        RedisSerializationContext.SerializationPair<String> serializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(valueSerialization);
        String _defaultConfig = Strings.isNullOrEmpty(defaultConfig) ? DEFAULTCONFIG_VALUE : defaultConfig;
        Map<String, String> values = Splitter.on(',').withKeyValueSeparator('=').split(_defaultConfig);
        long ttl = MapUtils.getLongValue(values, "ttl", 0);
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl == 0L ? Duration.ZERO : Duration.ofSeconds(ttl))
                .disableCachingNullValues().disableKeyPrefix().serializeValuesWith(serializationPair);
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(cacheConfig).transactionAware().build();
    }

    private String defaultConfig;
    private List<String> cacheConfigs;

    private RedisCacheConfiguration buildCacheConfig(String config) {
        Map<String, String> values = Splitter.on(',').withKeyValueSeparator('=').split(config);
        long ttl = MapUtils.getLongValue(values, "ttl", 0);
        boolean nullValue = MapUtils.getBooleanValue(values, "nullValue", false);
        String preifx = MapUtils.getString(values, "prefix", null);
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl == 0L ? Duration.ZERO : Duration.ofSeconds(ttl));
        if (!nullValue) cacheConfig.disableCachingNullValues();
        if (Strings.isNullOrEmpty(preifx)) {
            cacheConfig.disableKeyPrefix();
        } else {
            cacheConfig.prefixKeysWith(preifx);
        }
    }

}
