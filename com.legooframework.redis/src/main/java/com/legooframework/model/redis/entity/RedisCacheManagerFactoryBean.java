package com.legooframework.model.redis.entity;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Map;

public class RedisCacheManagerFactoryBean extends AbstractFactoryBean<RedisCacheManager> {

    private RedisCacheConfiguration defaultCacheConfiguration;
    private final static String DEFAULTCONFIG_VALUE = "ttl=600,nullValue=false,prefix=null";

    @Override
    public Class<RedisCacheManager> getObjectType() {
        return RedisCacheManager.class;
    }

    @Override
    protected RedisCacheManager createInstance() throws Exception {
        RedisSerializationContext.SerializationPair<Object> serializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(valueSerialization);
        String _defaultConfig = Strings.isNullOrEmpty(defaultConfig) ? DEFAULTCONFIG_VALUE : defaultConfig;
        Map<String, String> values = Splitter.on(',').withKeyValueSeparator('=').split(defaultConfig);
        long ttl = MapUtils.getLongValue(values, "ttl", 0);
        boolean nullValue = MapUtils.getBooleanValue(values, "nullValue", true);
        String preifx = MapUtils.getString(values, "prefix", null);

        return null;
    }


    private RedisConnectionFactory redisConnectionFactory;
    private MultipleValueSerializer valueSerialization;
    private String defaultConfig;

    public void setValueSerialization(MultipleValueSerializer valueSerialization) {
        this.valueSerialization = valueSerialization;
    }
}
