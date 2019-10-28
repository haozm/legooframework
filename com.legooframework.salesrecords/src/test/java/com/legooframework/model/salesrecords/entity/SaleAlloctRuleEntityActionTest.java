package com.legooframework.model.salesrecords.entity;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.OrgEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-acp-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/covariant/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/salesrecords/spring-model-cfg.xml"}
)
public class SaleAlloctRuleEntityActionTest {

    @Before
    public void setUp() throws Exception {
        LoginContextHolder.setAnonymousCtx();
    }

    @Test
    public void createByStore() {
        StoEntity store = stoEntityAction.loadById(1120);
        List<SaleAlloctRuleEntity.Rule> memrule00 = Lists.newArrayList();
        memrule00.add(SaleAlloctRuleEntity.Rule.serviceEmp(1));
        List<SaleAlloctRuleEntity.Rule> memrule01 = Lists.newArrayList();

        memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));
        memrule01.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));

        List<SaleAlloctRuleEntity.Rule> memrule02 = Lists.newArrayList();
        memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        memrule02.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));

        List<List<SaleAlloctRuleEntity.Rule>> memner_rule = Lists.newArrayList();
        memner_rule.add(memrule00);
        memner_rule.add(memrule01);
        memner_rule.add(memrule02);

        List<List<SaleAlloctRuleEntity.Rule>> no_memner_rule = Lists.newArrayList();
        List<SaleAlloctRuleEntity.Rule> no_memrule01 = Lists.newArrayList();

        no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));

        List<SaleAlloctRuleEntity.Rule> no_memrule02 = Lists.newArrayList();
        no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        no_memner_rule.add(no_memrule01);
        no_memner_rule.add(no_memrule02);


        List<List<SaleAlloctRuleEntity.Rule>> crs_memner_rule = Lists.newArrayList();
        List<SaleAlloctRuleEntity.Rule> crs_memrule00 = Lists.newArrayList();
        crs_memrule00.add(SaleAlloctRuleEntity.Rule.serviceEmp(1));
        List<SaleAlloctRuleEntity.Rule> crs_memrule01 = Lists.newArrayList();

        crs_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        crs_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));
        crs_memrule01.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));

        List<SaleAlloctRuleEntity.Rule> crs_memrule02 = Lists.newArrayList();
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));
        crs_memner_rule.add(crs_memrule00);
        crs_memner_rule.add(crs_memrule01);
        crs_memner_rule.add(crs_memrule02);

        List<List<SaleAlloctRuleEntity.Rule>> crs_no_memner_rule = Lists.newArrayList();
        List<SaleAlloctRuleEntity.Rule> csr_no_memrule01 = Lists.newArrayList();

        csr_no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        csr_no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));

        List<SaleAlloctRuleEntity.Rule> csr_no_memrule02 = Lists.newArrayList();
        csr_no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        csr_no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        csr_no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        crs_no_memner_rule.add(no_memrule01);
        crs_no_memner_rule.add(no_memrule02);

        OrgEntity company = orgEntityAction.loadComById(1);
        SaleAlloctRuleEntity rule = SaleAlloctRuleEntity.createByCompany(company, true, memner_rule,
                no_memner_rule, crs_memner_rule, crs_no_memner_rule);
        System.out.println(rule.toString());
    }

    @Test
    public void findByStore() {
        StoEntity store = stoEntityAction.loadById(1120);
        saleAlloctRuleEntityAction.findByStore(store).ifPresent(x -> System.out.println(x));
    }

    @Test
    public void loadAllByCompany() {
        OrgEntity company = orgEntityAction.loadComById(1);
        saleAlloctRuleEntityAction.loadAllByCompany(company.getId()).ifPresent(x -> System.out.println(x));
        System.out.println("------------------------------");
        saleAlloctRuleEntityAction.loadAllByCompany(company.getId()).ifPresent(x -> System.out.println(x));
    }

    @Test
    public void insert4Store() {
        StoEntity store = stoEntityAction.loadById(1120);
        List<SaleAlloctRuleEntity.Rule> memrule00 = Lists.newArrayList();
        memrule00.add(SaleAlloctRuleEntity.Rule.serviceEmp(1));
        List<SaleAlloctRuleEntity.Rule> memrule01 = Lists.newArrayList();

        memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));
        memrule01.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));

        List<SaleAlloctRuleEntity.Rule> memrule02 = Lists.newArrayList();
        memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        memrule02.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));

        List<List<SaleAlloctRuleEntity.Rule>> memner_rule = Lists.newArrayList();
        memner_rule.add(memrule00);
        memner_rule.add(memrule01);
        memner_rule.add(memrule02);

        List<List<SaleAlloctRuleEntity.Rule>> no_memner_rule = Lists.newArrayList();
        List<SaleAlloctRuleEntity.Rule> no_memrule01 = Lists.newArrayList();

        no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));

        List<SaleAlloctRuleEntity.Rule> no_memrule02 = Lists.newArrayList();
        no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        no_memner_rule.add(no_memrule01);
        no_memner_rule.add(no_memrule02);


        List<List<SaleAlloctRuleEntity.Rule>> crs_memner_rule = Lists.newArrayList();
        List<SaleAlloctRuleEntity.Rule> crs_memrule00 = Lists.newArrayList();
        crs_memrule00.add(SaleAlloctRuleEntity.Rule.serviceEmp(1));
        List<SaleAlloctRuleEntity.Rule> crs_memrule01 = Lists.newArrayList();

        crs_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        crs_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));
        crs_memrule01.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));

        List<SaleAlloctRuleEntity.Rule> crs_memrule02 = Lists.newArrayList();
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        crs_memrule02.add(SaleAlloctRuleEntity.Rule.serviceEmp(0.22));
        crs_memner_rule.add(crs_memrule00);
        crs_memner_rule.add(crs_memrule01);
        crs_memner_rule.add(crs_memrule02);

        List<List<SaleAlloctRuleEntity.Rule>> crs_no_memner_rule = Lists.newArrayList();
        List<SaleAlloctRuleEntity.Rule> csr_no_memrule01 = Lists.newArrayList();

        csr_no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        csr_no_memrule01.add(SaleAlloctRuleEntity.Rule.saledEmp(0.44));

        List<SaleAlloctRuleEntity.Rule> csr_no_memrule02 = Lists.newArrayList();
        csr_no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.33));
        csr_no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        csr_no_memrule02.add(SaleAlloctRuleEntity.Rule.saledEmp(0.11));
        crs_no_memner_rule.add(no_memrule01);
        crs_no_memner_rule.add(no_memrule02);
        OrgEntity company = orgEntityAction.loadComById(1);
        saleAlloctRuleEntityAction.insert4Company(company, true, memner_rule, no_memner_rule, crs_memner_rule,
                crs_no_memner_rule, true);
    }


    @Autowired
    private OrgEntityAction orgEntityAction;
    @Autowired
    private StoEntityAction stoEntityAction;
    @Autowired
    private SaleAlloctRuleEntityAction saleAlloctRuleEntityAction;
}