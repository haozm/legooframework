package com.legooframework.model.organization.entity;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EmpParamChecker {
	
	private static final Map<String,String> params = Maps.newConcurrentMap();
	private static final Set<String> addRequireParam = Sets.newConcurrentHashSet();
	private static final Set<String> addNotNullOrEmptyParam = Sets.newConcurrentHashSet();
	
	private EmpParamChecker() {
		
	}
	
	static {
		params.put("id", "职员编号");
		params.put("workNo", "职员工作号");
		params.put("userName", "职员名称");
		params.put("userSex", "职员性别");
		params.put("userSexName", "职员性别名称");
		params.put("userBirthday", "职员生日");
		params.put("userRemark", "职员备注");
		params.put("comWorkStatus", "职员工作状态");
		params.put("comWorkStatusName", "职员工作状态名称");
		params.put("phoneNo", "职员电话");
		params.put("employeeTime", "职员入职时间");
		params.put("placeId", "职员籍贯");
		params.put("location", "职员详细地址");
		params.put("accountId", "职员登录账号");
		params.put("orgId", "职员组织");
		params.put("storeId", "职员所在门店");
		params.put("companyId", "职员所在公司");
		params.put("roleNos", "职员角色");
		doAddRequireParam();
		doAddNotNullOrEmptyParam();
	}
	
	private static void doAddRequireParam() {
		addRequireParam.clear();
		addRequireParam.add("workNo");
		addRequireParam.add("userName");
		addRequireParam.add("userSex");
		addRequireParam.add("userBirthday");
		addRequireParam.add("orgId");
		addRequireParam.add("storeId");
		addRequireParam.add("roleNos");
	}
	
	private static void doAddNotNullOrEmptyParam() {
		addNotNullOrEmptyParam.clear();
		addNotNullOrEmptyParam.add("workNo");
		addNotNullOrEmptyParam.add("userSex");
		addNotNullOrEmptyParam.add("userName");
		addNotNullOrEmptyParam.add("phoneNo");
		addNotNullOrEmptyParam.add("orgId");
		addNotNullOrEmptyParam.add("roleNos");
	}
	
	public static void checkAddParams(Map<String,Object> datas) {
		addRequireParam.forEach(x -> {
			if(!datas.keySet().contains(x))
				throw new IllegalArgumentException(String.format("输入的参数中缺少[%s]", params.get(x))); 
		});
		addNotNullOrEmptyParam.forEach(x ->{
			if(Strings.isNullOrEmpty((String)datas.get(x)))
				throw new IllegalArgumentException(String.format("输入的参数[%s]不能为空", params.get(x)));
		});
	}
	
}
