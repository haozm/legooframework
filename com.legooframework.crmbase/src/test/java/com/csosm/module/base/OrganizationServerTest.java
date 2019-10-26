package com.csosm.module.base;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.entity.OrgTreeViewDto;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.google.common.base.Optional;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class OrganizationServerTest {

//	@Test
//	public void testSaveOrganization() {
//		LoginUserContext loginUser = LoginUserContext.anonymous();
//		System.out.println(ResourceUtils.CLASSPATH_URL_PREFIX);
//		orgServer.saveOrganization(loginUser, 75, "广州区域1", "gzqy1", 2);
//		orgServer.saveOrganization(loginUser, 75, "广州区域2", "gzqy2", 2);
//		orgServer.saveOrganization(loginUser, 75, "广州区域3", "gzqy3", 2);
//	}
//
//	@Test
//	public void testEditOrganization() {
//		LoginUserContext loginUser = LoginUserContext.anonymous();
////		orgServer.editOrganization(loginUser, 74, "改变名称", "cszz", 1, 0);
//		orgServer.editOrganization(loginUser, 71, "深圳测试莱特妮丝1", "szcsltns", 1, 0, 1, 1);
//	}

	@Test
	public void testRemoveOrganization() {
		LoginUserContext user = baseServer.loadByUserName(2, "cs001");
		orgServer.removeOrganization(user, 49);
	}

	@Test
	public void testSwitchOrganization() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		orgServer.switchOrganization(loginUser, 71, 75);
	}
	
	@Test
	public void testLoadOrg() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		System.out.println(action.loadById(1).toMap());
	}

	@Test
	public void testLoadOrg2() {
		LoginUserContext loginUser = baseServer.loadByUserName(1, "dgqx");
		Optional<OrgTreeViewDto> rootDto = baseServer.loadRootTreeWithEmps(loginUser);
		System.out.println(rootDto);
		Optional<List<OrgTreeViewDto>> treeDto = baseServer
				.loadOrgTreeWithEmpsByOrgId(13, loginUser);
		System.out.println(treeDto);
		Optional<List<OrgTreeViewDto>> storeDto = baseServer
				.loadOrgTreeWithEmpByStoreId(32, loginUser);
		System.out.println(storeDto);
	}
	@Autowired
	private BaseModelServer baseServer;
	@Autowired
	private OrganizationServer orgServer;
	@Autowired
	private OrganizationEntityAction action;
}
