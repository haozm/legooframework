package com.legooframework.model.salesrecords.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/batchsupport/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml"}
)
public class SaleRecord4EmployeeEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void findByStore() {
        StoEntity store = stoEntityAction.loadById(25);
        Optional<SaleAlloctRule4Store> saleAlloctRule4Store = saleAlloctRuleEntityAction.findByStore4Use(store);
        Optional<List<SaleRecord4EmployeeEntity>> todo_list = saleRecord4EmployeeEntityAction.findByStore(store);
        List<SaleAlloct4EmpResult> saleAlloct4EmpResults = Lists.newArrayList();
        for (SaleRecord4EmployeeEntity $it : todo_list.get()) {
            SaleAlloct4EmpResult res = new SaleAlloct4EmpResult($it);
            saleAlloctRule4Store.get().allocation(res);
            saleAlloct4EmpResults.add(res);
        }
        List<SaleAlloctResultEntity> saves = Lists.newArrayList();
        saleAlloct4EmpResults.forEach(x -> saves.addAll(x.processResult()));
        saleAlloctResultEntityAction.batchInsert(saves);
    }

    @Test
    public void findUndoCountByCompany() {
        OrgEntity com = orgEntityAction.loadComById(1);
        System.out.println(saleRecord4EmployeeEntityAction.loadUndoCountByCompany(com));
    }

    @Test
    public void findUndoByCompany() {
        OrgEntity com = orgEntityAction.loadComById(1);
        saleRecord4EmployeeEntityAction.findUndoByCompany(com).ifPresent(x -> System.out.println(x.size()));
    }

    @Test
    public void findByStoreWithPeriod() {
        StoEntity store = stoEntityAction.loadById(25);
        LocalDate start = LocalDate.now();
        saleRecord4EmployeeEntityAction.findByStoreWithPeriod(store, start.plusDays(-100), start);
    }

    @Test
    public void findById() {
        saleRecord4EmployeeEntityAction.findById(4205625).ifPresent(x -> System.out.println(x));
    }

    @Autowired
    private OrgEntityAction orgEntityAction;
    @Autowired
    SaleAlloctResultEntityAction saleAlloctResultEntityAction;
    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private SaleAlloctRuleEntityAction saleAlloctRuleEntityAction;
    @Autowired
    private SaleRecord4EmployeeEntityAction saleRecord4EmployeeEntityAction;
}