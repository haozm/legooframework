package com.csosm.module.menu;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.menu.entity.ResourceDto;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class SecAccessServiceTest {

    @Test
    public void loadResByAccount() {
        Optional<OrganizationEntity> com = organizationEntityAction.findCompanyById(1);
        secAccessService.authorized(com.get(), 3, (String) null);
    }

    @Test
    public void loadResByAccount02() {
        Optional<OrganizationEntity> com = organizationEntityAction.findCompanyById(1);
        secAccessService.authorized(com.get(), 3, "page-010", "page-011", "page-012");
    }

    @Test
    public void authorized() {
    }
    
    @Test
	public void testLoadMenu() {
    	LoginUserContext user = baseAction.loadByUserName(1, "dgqx050");
		java.util.Optional<ResourceDto> resource = secAccessService.loadResByAccount(user);
		System.out.println(resource);
	}

	@Autowired
	private BaseModelServer baseAction;
	
    @Autowired
    OrganizationEntityAction organizationEntityAction;
    
    @Autowired
    SecAccessService secAccessService;
}