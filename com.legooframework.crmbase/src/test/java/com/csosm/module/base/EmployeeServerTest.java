package com.csosm.module.base;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.entity.EmployeeEntityAction;
import com.csosm.module.base.entity.OrgTreeViewDto;
import com.csosm.module.query.QueryEngineService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
                ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml"}
)
public class EmployeeServerTest {

	@Test
	public void testSaveEmployee() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
//		empServer.saveOrgEmployee(loginUser, 13, "13800138040", "小熊3", "13800138001", 1, Lists.newArrayList(7));
		empServer.saveStoreEmployee(loginUser, 32, "13800138050", "小熊经理1", "13800138000", 1, Lists.newArrayList(5,7));
	}

	@Test
	public void testRemoveEmployees() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
//		empServer.removeStoreEmployees(loginUser, Lists.newArrayList(4138,4137));
	}

	@Test
	public void testEnableEmployees() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
//		empServer.enableStoreEmployees(loginUser, Lists.newArrayList(4138,4137));
	}

	@Test
	public void testDisableEmployees() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		
	}
	
	@Test
	public void testResetPwd() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		//empServer.resetOrgEmployeePwd(loginUser,13,Lists.newArrayList(4155));
	}
	
	@Test
	public void testEditEmployee() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
		empServer.editStoreEmployee(loginUser,1462, 4167, "改变1", "123", 2, Lists.newArrayList(4,5,7));
	}
	
	@Test
	public void saveComEmployee() {
		LoginUserContext loginUser = LoginUserContext.anonymous();
	//	baseServer.registerCompany(loginUser, "小熊公司", "xxgs", "xiaoxiong1");
	}
	
	@Test
	public void testLoginUser() {
		LoginUserContext user = baseServer.loadByUserName(1, "13800138045");
		System.out.println(user);
	}
	
	@Test
	public void testChangePwd() {
//		LoginUserContext loginUser = LoginUserContext.anonymous();
		LoginUserContext user = baseServer.loadByUserName(1, "13800138045");
		//empServer.changePassword(user, "12345678", "88888888","88888888");
	}
	
	@Test
	public void testLoadOrg() {
		LoginUserContext loginUser = baseServer.loadByUserName(109, "xxxcs");
		System.out.println(loginUser.toMap());
//		Optional<OrgTreeViewDto> treeDto = baseServer.loadOrgTreeWithStoreByOrgId(
//				loginUser.getOrganization().isPresent() ? loginUser.getOrganization().get().getId()
//						: loginUser.getCompany().get().getId(),
//				loginUser);
//		System.out.println(treeDto);
		Integer orgId = null;
		if(loginUser.getCompany().isPresent()){
			orgId = loginUser.getCompany().get().getId();
		}else if(loginUser.getOrganization().isPresent()) {
			orgId = loginUser.getOrganization().get().getId();
		} 
		Preconditions.checkState(null == orgId, "登录用户无组织信息");
		Map<String, Object> params = Maps.newHashMap();
		params.put("orgId", orgId);
//		Optional<List<Map<String, Object>>> managersOpt = getBean(QueryEngineService.class, request)
//				.queryForList("employee", "load_managers", params);
	}
	
	@Test
	public void testLoadOrg2() {
		LoginUserContext loginUser = baseServer.loadByUserName(109, "xxxcs");
//		Optional<OrgTreeViewDto> treeDto = baseServer
//				.loadOrgTreeWithEmpByOrgId(109, loginUser);
		//System.out.println(treeDto);
	}
	
	@Test
	public void testEnableEmp() {
		LoginUserContext loginUser = baseServer.loadByUserName(1, "wnd_1");
		empAction.disableEmployees(Lists.newArrayList(2313), loginUser.getCompany().get());
	}
	
	@Test
	public void testFireEmp() {
		LoginUserContext loginUser = baseServer.loadByUserName(1, "dgqx050");
		empServer.fireEmployees(1, Lists.newArrayList(12,67,132));
	}
	
	@Test
	public void testswitchEmps() {
		LoginUserContext loginUser = baseServer.loadByUserName(1, "dymtj");
		empServer.switchStore(loginUser, 2,1, 19);
	}
	
	@Test
	public void testRecoverEmps() {
		LoginUserContext loginUser = baseServer.loadByUserName(1, "dgqx050");
		//empServer.switchEmployees(loginUser, 1125, 1120, Lists.newArrayList(8317,8320));
	}
	@Autowired
	private EmployeeServer empServer;
	
	@Autowired
	private BaseModelServer baseServer;
	
	@Autowired
	private EmployeeEntityAction empAction;
	
}
