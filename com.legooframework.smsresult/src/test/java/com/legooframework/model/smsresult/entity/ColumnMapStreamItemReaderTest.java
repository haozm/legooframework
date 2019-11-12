package com.legooframework.model.smsresult.entity;

import com.legooframework.model.core.jdbc.sqlengine.ColumnMapStreamItemReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class ColumnMapStreamItemReaderTest implements ApplicationContextAware {

    @Test
    public void doreazd() throws Exception {
        ItemReader<Map<String, Object>> reader = app.getBean("sms2HourSyncStreamItemReader", ColumnMapStreamItemReader.class);
        Map<String, Object> item = null;
        for (; ; ) {
            item = reader.read();
            System.out.println(item);
            if (item == null) break;
        }
        ItemReader<Map<String, Object>> reader02 = app.getBean("sms2HourSyncStreamItemReader", ColumnMapStreamItemReader.class);
        item = null;
        for (; ; ) {
            item = reader02.read();
            System.out.println(item);
            if (item == null) break;
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.app = applicationContext;
    }

    ApplicationContext app;
}
