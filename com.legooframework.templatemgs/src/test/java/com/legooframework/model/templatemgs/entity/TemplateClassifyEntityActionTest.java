package com.legooframework.model.templatemgs.entity;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db2-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/templatemgs/spring-model-cfg.xml"}
)
public class TemplateClassifyEntityActionTest {

    @Test
    public void loadByCompany() {
        LoginContextHolder.setAnonymousCtx();
        CrmOrganizationEntity com = organizationAction.findCompanyById(1).get();
        Optional<List<TemplateClassifyEntity>> res = classifyEntityAction.loadByCompany(com);
        res.ifPresent(System.out::println);
    }

    @Test
    public void loadTreeNodeByCompany() {
        LoginContextHolder.setAnonymousCtx();
        CrmOrganizationEntity com = organizationAction.findCompanyById(1).get();
        Optional<List<TreeNode>> res = classifyEntityAction.loadTreeNodeByCompany(com);
        res.ifPresent(System.out::println);
    }

    @Autowired
    CrmOrganizationEntityAction organizationAction;
    @Autowired
    TemplateClassifyEntityAction classifyEntityAction;
}