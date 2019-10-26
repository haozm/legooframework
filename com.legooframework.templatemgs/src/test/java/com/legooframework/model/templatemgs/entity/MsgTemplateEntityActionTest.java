package com.legooframework.model.templatemgs.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.crmadapter.service.CrmReadService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-template-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/core/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmadapter/spring-model-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/templatemgs/spring-model-cfg.xml"}
)
public class MsgTemplateEntityActionTest {

    @Test
    public void insertGr() {
        LoginContextHolder.setAnonymousCtx();
        Optional<CrmOrganizationEntity> company = organizationAction.findCompanyById(1);
        Preconditions.checkState(company.isPresent());
        Optional<TemplateClassifyEntity> classify = classifyEntityAction.loadById("2011");
        Preconditions.checkState(classify.isPresent());
        List<UseScope> scopeList = Lists.newArrayList(UseScope.SmsMsg, UseScope.WxMsg);

//        for (int i = 0; i < 10; i++) {
//            MsgTemplateEntity instance = new MsgTemplateEntity(String.format("通用模板 %s", i), classify.get(), scopeList);
//            msgTemplateEntityAction.insert(instance);
//        }
    }

    @Test
    public void loadDefaultByClassifies() {
        LoginContextHolder.setAnonymousCtx();
        Optional<CrmOrganizationEntity> company = organizationAction.findCompanyById(100098);
        msgTemplateEntityAction.loadDefaultByClassifies( Lists.newArrayList("TOUCH90_100098_0_15", "TOUCH90_100098_0_90"));
    }

    @Test
    public void insertCom() {
        LoginContextHolder.setAnonymousCtx();
        Optional<CrmOrganizationEntity> company = organizationAction.findCompanyById(1);
        Preconditions.checkState(company.isPresent());
        Optional<TemplateClassifyEntity> classify = classifyEntityAction.loadById("2011");
        Preconditions.checkState(classify.isPresent());
        List<UseScope> scopeList = Lists.newArrayList(UseScope.SmsMsg, UseScope.WxMsg);
//        for (int i = 0; i < 10; i++) {
//            MsgTemplateEntity instance = new MsgTemplateEntity(company.get(), String.format("实测是啊啥的啊是  %s", i), classify.get(), scopeList);
//            msgTemplateEntityAction.insert(instance);
//        }

    }

    @Test
    public void insertStore() {
        LoginContextHolder.setAnonymousCtx();
        Optional<CrmOrganizationEntity> company = organizationAction.findCompanyById(1);
        Preconditions.checkState(company.isPresent());

        Optional<CrmStoreEntity> stre = storeEntityAction.findById(company.get(), 12);
        Optional<TemplateClassifyEntity> classify = classifyEntityAction.loadById("2011");
        Preconditions.checkState(classify.isPresent());
        List<UseScope> scopeList = Lists.newArrayList(UseScope.SmsMsg, UseScope.WxMsg);
//        for (int i = 0; i < 10; i++) {
//            MsgTemplateEntity instance = new MsgTemplateEntity(stre.get(), String.format("风的样子%s", i), classify.get(), scopeList);
//            msgTemplateEntityAction.insert(instance);
//        }

    }

    @Test
    public void findById() {
        LoginContextHolder.setAnonymousCtx();
        Optional<MsgTemplateEntity> msgt = msgTemplateEntityAction.findById("7loFBq7k8gUPATqX");
        msgt.ifPresent(x -> System.out.println(x));
    }

    @Test
    public void black() {
        LoginUser user = crmReadService.loadByLoginName(1, "dgqx756_1");
        System.out.println(user.toString());
        LoginContextHolder.setCtx(user);
        msgTemplateEntityAction.blackTemplate("5C3P1ipWmCkvKose");
    }

    @Test
    public void change() {
        LoginUser user = crmReadService.loadByLoginName(1, "dgqx756_1");
        System.out.println(user.toString());
        LoginContextHolder.setCtx(user);
        //msgTemplateEntityAction.changeTemplate("ze89nycTPlBSbYCJ", "asjdoasniahsdoasdsad");
    }


    @Test
    public void loadEnabledListByCom() {
        LoginContextHolder.setAnonymousCtx();
        Optional<CrmOrganizationEntity> com = organizationAction.findCompanyById(100098);
        Optional<List<MsgTemplateEntity>> list = msgTemplateEntityAction.loadEnabledListByCom(com.get());
        String prefix = String.format("TOUCH90_100098_%s_", 0);
        List<MsgTemplateEntity> def_list = list.get().stream().filter(MsgTemplateEntity::isDefaulted)
                .filter(x -> StringUtils.startsWith(x.getSingleClassifies(), prefix))
                .collect(Collectors.toList());
        Optional<MsgTemplateEntity> entity_opt = def_list.stream().filter(MsgTemplateEntity::isCompany).findFirst();
    }

    @Autowired
    CrmOrganizationEntityAction organizationAction;
    @Autowired
    CrmStoreEntityAction storeEntityAction;
    @Autowired
    TemplateClassifyEntityAction classifyEntityAction;
    @Autowired
    MsgTemplateEntityAction msgTemplateEntityAction;
    @Autowired
    CrmReadService crmReadService;
}