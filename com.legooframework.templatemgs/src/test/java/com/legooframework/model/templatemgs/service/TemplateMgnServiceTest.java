package com.legooframework.model.templatemgs.service;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.crmadapter.service.CrmReadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-template-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/templatemgs/spring-model-cfg.xml"}
)
public class TemplateMgnServiceTest {

    @Test
    public void loadTreeNodeByCompany() {
        LoginContextHolder.setAnonymousCtx();
//        TreeNode tr = templateMgnService.loadTreeNodeByCompany(1);
//        System.out.println(tr);
    }

    @Test
    public void loadEnabledTouch90Template() {
        LoginUser user = crmReadService.loadByLoginName(1, "00006HBDYMTJ");
        LoginContextHolder.setCtx(user);
        //templateMgnService.loadEnabledTouch90Template(user);
    }

    @Test
    public void loadDefaultTouch90Template() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        templateMgnService.loadDefaultTouch90Template(company.get(), null);
    }

    @Test
    public void loadDefaultTouch90TemplateWithStore() {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Optional<CrmOrganizationEntity> company = organizationEntityAction.findCompanyById(100098);
        Optional<CrmStoreEntity> store = storeEntityAction.findById(company.get(), 1315);
        templateMgnService.loadDefaultTouch90Template(company.get(), store.get());
    }

    @Autowired
    CrmStoreEntityAction storeEntityAction;
    @Autowired
    CrmOrganizationEntityAction organizationEntityAction;
    @Autowired
    CrmReadService crmReadService;
    @Autowired
    TemplateMgnService templateMgnService;
}