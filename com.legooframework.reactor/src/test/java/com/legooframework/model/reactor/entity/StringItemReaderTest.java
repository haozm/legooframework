package com.legooframework.model.reactor.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/reactor/spring-model-cfg.xml"}
)
public class StringItemReaderTest implements ApplicationContextAware {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
    }

    @Test
    public void read() throws Exception {
        Job testJob = appCtx.getBean("retailSmsJob", Job.class);
        JobParametersBuilder jb = new JobParametersBuilder();
        jb.addString("job.params", String.format("sql=query4RetailSmsJob$stmtId=%s$companyIds=%s", "RetailFactEntity.query4list",
                "1,2,3,4,5,6"));
        jb.addDate("job.tamptime", LocalDateTime.now().toDate());
        JobParameters jobParameters = jb.toJobParameters();
        jobLauncher.run(testJob, jobParameters);
    }

    @Test
    public void fails() throws Exception {
        Set<Long> executions = jobOperator.getRunningExecutions("testJob");
        jobOperator.stop(executions.iterator().next());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCtx = applicationContext;
    }

    private ApplicationContext appCtx;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

}