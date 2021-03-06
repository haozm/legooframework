package com.legooframework.model.core.jdbc.sqlengine;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statement/spring-model-cfg.xml"}
)
public class SQLStatementFactoryTest {

    @Test
    public void findStmtById() throws Exception {
        Optional<SQLStatement> asd = statementFactory.findStmtById("store", "loadByOrgId");
        Assert.assertTrue(asd.isPresent());
    }

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext(ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-nodb-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/statement/spring-model-cfg.xml");
        try {
            Thread.sleep(1000 * 60 * 3);
        } catch (Exception e) {

        }
    }

    @Test
    public void getExecSql() {
    }

    @Autowired
    private SQLStatementFactory statementFactory;
}