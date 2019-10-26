package com.legooframework.model.amqp.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-ampq-cfg.xml"}
)
public class SimpleJdbcInsertLearnTest {

    @Test
    public void singleInsert() {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withCatalogName("acp").withTableName("MY_TEST_TABLE").usingGeneratedKeyColumns("id");
        Map<String, Object> params = Maps.newHashMap();
        params.put("key_word", "nihao");
        params.put("enabled", 1);
        params.put("delete_flag", 0);
        //Number pk = jdbcInsert.executeAndReturnKey(params);
        //System.out.println(pk.intValue());

        List<Map<String, Object>> mapList = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> $it = Maps.newHashMap();
            $it.put("key_word", String.format("nihao %s", i));
            $it.put("enabled", 1);
            $it.put("delete_flag", 0);
            mapList.add($it);
        }

        int[] ints = jdbcInsert.executeBatch(mapList.toArray(new Map[0]));
        System.out.println(Arrays.toString(ints));
    }

    @Autowired
    private DataSource dataSource;
}