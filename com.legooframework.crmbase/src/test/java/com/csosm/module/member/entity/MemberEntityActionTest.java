package com.csosm.module.member.entity;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.EmployeeEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.member.MemberServer;
import com.csosm.module.menu.SecAccessService;
import com.csosm.module.menu.entity.ResourceDto;
import com.csosm.module.query.QueryEngineService;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/junit/spring-db-cfg.xml",
		ResourceUtils.CLASSPATH_URL_PREFIX + "META-INF/crmbase/spring-model-cfg.xml" })
public class MemberEntityActionTest {

	@Test
	public void test() {
		StoreEntity store = storeAction.loadById(22);
		memberAction.enableMembers(store, Lists.newArrayList(1369235));
		// System.out.println(findMembersByIds);
	}

	@Test
	public void testAddMember() {
		LoginUserContext user = baseAction.loadByUserName(1, "00002HBDYMTJ");
		System.out.println(user.toMap());
//		StoreEntity store = storeAction.loadById(22);
//		user.setStore(store);
//		memberAction.addSimpleMember(user, "小熊测试", 1, "13801010210", 1, user.getEmployee());
	}

	@Test
	public void testDisable() {
		StoreEntity store = storeAction.loadById(22);
		memberAction.disableMembers(store, Lists.newArrayList(1369235));
		// System.out.println(findMembersByIds);
	}

	@Test
	public void testAllot() {
		StoreEntity store = storeAction.loadById(22);
		LoginUserContext user = baseAction.loadByUserName(1, "dgqx");
		memberAction.allot(store, Lists.newArrayList(1369235), user.getEmployee());
	}

	@Test
	public void testNoAllow() {
		StoreEntity store = storeAction.loadById(22);
		memberAction.deallocate(store, Lists.newArrayList(1369235));
	}

	@Test
	public void testToLunar() {
		StoreEntity store = storeAction.loadById(22);
		memberAction.toLunarBirthDay(store, Lists.newArrayList(953, 995, 2037, 60905));
	}

	@Test
	public void testActivePhone() {
		StoreEntity store = storeAction.loadById(22);
		memberAction.activateMobilePhone(store, Lists.newArrayList(953, 995, 2037, 60905));
	}

	@Test
	public void testTransformMembers() {
		StoreEntity store = storeAction.loadById(22);
		Optional<List<EmployeeEntity>> dest = empAction.loadEmployeesByStore(store, Lists.newArrayList(132));
		Optional<List<EmployeeEntity>> scource = empAction.loadEmployeesByStore(store, Lists.newArrayList(2390));
		memberAction.transferMembers(scource.get().get(0), dest.get().get(0));
	}

	@Test
	public void testAddDetailMember() {
		LoginUserContext user = baseAction.loadByUserName(1, "dgqx311_1");
		StoreEntity store = storeAction.loadById(22);
	}

	@Test
	public void testEditMember() {
		LoginUserContext user = baseAction.loadByUserName(1, "dgqx311_1");
		StoreEntity store = storeAction.loadById(22);
		memberAction.editMemberBase(user, 1369238, "xg_iconUrl", "xg_名称", "xg_短拼", 2, new Date(), new Date(), 2, 1, 1, "xg_email", "xg_13800138000", "xg_020_291810231", "xg_qq", "xg_wbo", "xg_wxId");
		memberAction.editMemberAddition(store, 1369238, 1, "xg_idol", "xg_carePeople", 12, 1, "xg_jobType", 4, "xg_hobby", "xg_likeBrand", 1, "xg_specialDay", 1, 1, "xg_certificate", 2, "xg_detailAddress");
//		memberAction.editMemberCard(store, 1369238, 1, "xg_memberCardNum", new Date());
//		memberAction.editMemberCard(store, 1369238, 1, "xg_memberCardNum", new Date());
		;
	}
	
	
	@Test
	public void testMap() {
		LoginUserContext user = baseAction.loadByUserName(1, "00001HBDYMTJ");
		System.out.println(user);
//		Optional<List<Map<String, Object>>> queryForList = queryService.queryForList("member", "load_members", user.toMap());
//		System.out.println(queryForList);
	}
	
	@Test
	public void testMaps() {
//		LoginUserContext user = baseAction.loadByUserName(1, "dgqx050");
		Map<String,Object> params = Maps.newHashMap();
		params.put("storeId", 1);
		params.put("companyId", 1);
		params.put("hasRFM", 1);
		params.put("minR", 1);
		params.put("maxR", 2);
		params.put("minF", 3);
		params.put("maxF", 4);
		params.put("minM", 5);
		params.put("maxM", 6);
		Optional<List<Map<String, Object>>> queryForList = queryService.queryForList("member", "load_members", params);
		System.out.println(queryForList);
	}
	
	@Test
	public void testLoadMemberAssignCountList() {
		Map<String,Object> params = Maps.newHashMap();
		params.put("storeId", 1);
		params.put("USER_COMPANY_ID", 1);
		Optional<List<Map<String, Object>>> listOpt = queryService.queryForList("member", "count_assign_member", params);
		if(listOpt.isPresent()) System.out.println(listOpt.get());
	}
	
	@Test
	public void testOnekeyAssign() {
		memberServer.oneKeyAllotMembers(1);
	}
	
	@Test
	public void testCancelOnekeyAssign() {
		memberServer.deallocateOneKeyMembers(1);
	}
	
	@Test
	public void testTransforMembers() {
		memberServer.transferMembers(1,132, 67);
	}
	
	@Test
	public void testCancelAssign() {
		memberServer.deallocateMembers(1, 85);
	}
	
	@Test
	public void testAssignRandom() {
		String str = "132:3,142:2";
		List<AssignDTO> assigns = AssignDTO.valueOf(str);
		memberServer.assginRandomMembers(1, assigns);
	}
	


	@Autowired
	private MemberServer memberServer;
	@Autowired
	private MemberEntityAction memberAction;
	@Autowired
	private StoreEntityAction storeAction;
	@Autowired
	private EmployeeEntityAction empAction;
	@Autowired
	private BaseModelServer baseAction;
	@Autowired
	private QueryEngineService queryService;
	@Autowired
	private SecAccessService secService;
}
