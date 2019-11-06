package com.legooframework.model.redis.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/redis/spring-model-cfg.xml"}
)
public class RedisTemplateSupportTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }


    @Test
    public void testGetString() {
//        Optional<List<CareRecordEntity>> list = careRecordEntityAction.query4List();
//        if (list.isPresent()) {
//            for (CareRecordEntity $it : list.get()) {
//                System.out.println($it.serializer());
//                redisTemplate.opsForValue().setIfPresent(String.format("CareRecordEntity-%d", $it.getId()), $it.serializer());
//            }
//        }
    }

    @Test
    public void testList() {
//        redisTemplate.opsForList().
//        redisTemplate.opsForList().leftPush("list", "hao");
//        redisTemplate.opsForList().leftPush("list", "xiao");
//        redisTemplate.opsForList().leftPush("list", "jie");
//        redisTemplate.opsForList().leftPush("list", "nihao");
        //     System.out.println(redisTemplate.opsForList().size("list"));
        //     System.out.println(redisTemplate.opsForList().range("list", 0, 3));
    }

    @Test
    public void testHash() {

//        for (int i = 0; i < 100; i++) {
//            CacheEntity cacheEntity = new CacheEntity(i, String.format("HXJ-%d", i), i % 2, LocalDate.now(), String.format("GZ-%d", i));
//            redisTemplate.opsForHash().put(CacheEntity.class.getSimpleName(), String.format("key-%d", i), cacheEntity);
//        }

        System.out.println(redisTemplate.opsForHash().get(CacheEntity.class.getSimpleName(), "key-1"));
//        Optional<List<CareRecordEntity>> list = careRecordEntityAction.query4List();
//        if (list.isPresent()) {
//            for (CareRecordEntity $it : list.get()) {
//                System.out.println($it.serializer());
//                redisTemplate.opsForHash().put("CareRecordEntity", $it.getId().toString(), $it.serializer());
//            }
//        }
    }

    @Test
    public void testZSet() {
        for (int i = 0; i < 20; i++) {
            double vale = RandomUtils.nextInt(1, 1000);
            // redisTemplate.opsForZSet().add()
        }
    }


    @Autowired
    private RedisTemplate redisTemplate;
//   ßßßßate CareRecordEntityAction careRecordEntityAction;

}