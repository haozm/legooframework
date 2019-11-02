package com.legooframework.model.redis.entity;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/redis/spring-model-cfg.xml"}
)
public class RedisEntityCacheTest {

    @Test
    public void cacheEnity() {
        CacheEntity cacheEntity = new CacheEntity(1, "haoxiaojie", 2, LocalDate.now(), "guangz");
        System.out.println(cacheEntity.getClass().getSimpleName());
        System.out.println(cacheEntity.serializer());
        redisEntityCache.cacheEnity(cacheEntity);
    }

    @Test
    public void getCacheEnity() {
        redisEntityCache.getIfExits(1, CacheEntity.class).ifPresent(x -> System.out.println(x));
    }

    @Autowired
    RedisEntityCache redisEntityCache;
}