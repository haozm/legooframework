package com.legooframework.model.redis.entity;

import com.google.common.base.Strings;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

public class RedisCacheConfigurationFactoryBean extends AbstractFactoryBean<RedisCacheConfiguration> {

    @Override
    public Class<RedisCacheConfiguration> getObjectType() {
        return RedisCacheConfiguration.class;
    }

    @Override
    protected RedisCacheConfiguration createInstance() throws Exception {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        if (ttl != 0) configuration.entryTtl(Duration.ofSeconds(ttl));
        if (!Strings.isNullOrEmpty(prefix)) configuration.prefixKeysWith(prefix);
        configuration.disableCachingNullValues();
        if (serializationPair != null) configuration.serializeValuesWith(serializationPair);
        return configuration;
    }

    private long ttl;
    private String prefix;
    private RedisSerializationContext.SerializationPair<Object> serializationPair;

    public void setSerializationPair(RedisSerializationContext.SerializationPair<Object> serializationPair) {
        this.serializationPair = serializationPair;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
