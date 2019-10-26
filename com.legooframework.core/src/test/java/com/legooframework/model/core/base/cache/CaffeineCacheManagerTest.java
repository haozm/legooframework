package com.legooframework.model.core.base.cache;

import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statement/spring-cache-cfg.xml"}
)
public class CaffeineCacheManagerTest {
//    DefaultFileSystemManager
    @Test
    public void getCache() {
        Assert.assertNotNull(cacheManager);
        Assert.assertNotNull(cacheManager.getCache("cahceD"));
        Assert.assertNull(cacheManager.getCache("XXXXX"));
    }

    @Test
    public void getCacheNames() {
        System.out.println(cacheManager.getCacheNames());
    }

    @Autowired
    CacheManager cacheManager;
}