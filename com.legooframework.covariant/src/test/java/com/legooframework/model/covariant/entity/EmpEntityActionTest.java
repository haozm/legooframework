package com.legooframework.model.covariant.entity;

import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml"}
)
public class EmpEntityActionTest {

    @Before
    public void init() {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findAllByStore() {
        StoEntity stoEntity = stoEntityAction.loadById(1120);
        empEntityAction.findEmpsByStore(stoEntity);
    }

    @Test
    public void findStoreManagersByStore() {
        StoEntity stoEntity = stoEntityAction.loadById(1120);
        empEntityAction.findStoreManagersByStore(stoEntity);
    }

    @Test
    public void call() {
        Map<String, Object> params = Maps.newHashMap();
        Optional<String> asd = jdbcQuerySupport.queryForObject("DBStorage", "GetTaskNums", params, String.class);
        CallStorageData callStorageData = new CallStorageData("");
        callStorageData.setTotal(asd.get());
        System.out.println(callStorageData);
    }

    @Test
    public void callData() {
        Map<String, Object> params = Maps.newHashMap();
        Optional<String> asd = jdbcQuerySupport.queryForObject("DBStorage", "GetTaskList", params, String.class);
        CallStorageData callStorageData = new CallStorageData("");
        callStorageData.setDatas(asd.get());
        System.out.println(callStorageData);
    }


    @Resource(name = "covariantJdbcQuerySupport")
    private JdbcQuerySupport jdbcQuerySupport;
    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private EmpEntityAction empEntityAction;
}