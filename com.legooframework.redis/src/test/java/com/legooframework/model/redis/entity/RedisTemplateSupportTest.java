package com.legooframework.model.redis.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.takecare.entity.CareRecordEntity;
import com.legooframework.model.takecare.entity.CareRecordEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/takecare/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/redis/spring-model-cfg.xml"}
)
public class RedisTemplateSupportTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }


    @Test
    public void testGetString() {
        Optional<List<CareRecordEntity>> list = careRecordEntityAction.query4List();
        if (list.isPresent()) {
            for (CareRecordEntity $it : list.get()) {
                System.out.println($it.serializer());
                redisTemplate.opsForValue().setIfPresent(String.format("CareRecordEntity-%d", $it.getId()), $it.serializer());
            }
        }
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
        Optional<List<CareRecordEntity>> list = careRecordEntityAction.query4List();
        if (list.isPresent()) {
            for (CareRecordEntity $it : list.get()) {
                System.out.println($it.serializer());
                redisTemplate.opsForHash().put("CareRecordEntity", $it.getId().toString(), $it.serializer());
            }
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CareRecordEntityAction careRecordEntityAction;

}