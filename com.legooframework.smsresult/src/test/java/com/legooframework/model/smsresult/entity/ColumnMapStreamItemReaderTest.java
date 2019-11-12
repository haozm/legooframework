package com.legooframework.model.smsresult.entity;

import com.legooframework.model.core.jdbc.sqlengine.ColumnMapStreamItemReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-smsresult-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/entities/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/smsresult/spring-model-cfg.xml"}
)
public class ColumnMapStreamItemReaderTest {

    @Test
    public void doreazd() throws Exception {
        Map<String, Object> item;
        for (; ; ) {
            item = reader.read();
            System.out.println(item);
            if (item == null) return;
        }

    }

    @Autowired
    private ColumnMapStreamItemReader reader;
}
