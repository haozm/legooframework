package com.legooframework.model.redis.entity;

import com.legooframework.model.core.utils.GsonUtil;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Collection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/redis/spring-model-cfg.xml"}
)
public class RedisCacheManagerFactoryBeanTest {

    @Test
    public void createInstance() {
        System.out.println(redisCacheManager == null);
    }

    @Test
    public void getCacheNames() {
        Cache cache = redisCacheManager.getCache("Cactana");
        Cache c01 = redisCacheManager.getCache("tempCache");
        c01.put("tempCache-01","asdasdasdasd");
        Collection<String> cacheNames = redisCacheManager.getCacheNames();
        System.out.println(cacheNames);
    }


    @Test
    public void getCache() throws Exception {
        Cache cache = redisCacheManager.getCache("Cactana");
        CacheEntity cacheEntity = new CacheEntity(1, String.format("HXJ-%d", 1), 1 % 2, LocalDate.now(), String.format("GZ-%d", 1));
        cache.put(String.format("CacheEntity-%d", cacheEntity.getId()), GsonUtil.serialize(cacheEntity));
        String asd = cache.get(String.format("CacheEntity-%d", cacheEntity.getId()), String.class);
        CacheEntity ca = GsonUtil.deserialize(asd, CacheEntity.class);
        System.out.println(ca);
    }

    @Autowired
    RedisCacheManager redisCacheManager;
}