package com.csosm.module.base;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.google.common.base.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)

public class StoreServerTest {

	@Test
	public void testSaveStore() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		storeServer.saveStore(loginUser, 78, "门店4", "13800138012", 1, 1, "门店4地址");
	}

	@Test
	public void testEditStore() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		storeServer.editStore(loginUser, 2402, "改变名称", "123", 1, 1, "改变地址", 0);
	}

	@Test
	public void testSwitchStore() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		storeServer.switchStore(loginUser, 1, 0);
	}

	@Test
	public void testLoadTree() {
		Optional<OrganizationEntity> org_root_opt =
				orgAction.findById(13);
		Optional<List<StoreEntity>> strore_list_opt =
				storeAction.loadAllSubStoreByOrg(org_root_opt.get());
	}
	@Autowired
	private StoreServer storeServer;
	@Autowired
	private OrganizationServer orgServer;
	@Autowired
	private OrganizationEntityAction orgAction;
	@Autowired
	private StoreEntityAction storeAction;
}
