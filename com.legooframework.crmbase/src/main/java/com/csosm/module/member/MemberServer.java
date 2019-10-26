package com.csosm.module.member;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.EmployeeEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.member.entity.AssignDTO;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;

public class MemberServer extends AbstractBaseServer{
	
	private static final Logger logger = LoggerFactory.getLogger(MemberServer.class);
	
	private EmployeeEntity getExistEmployee(StoreEntity store,Integer employeeId) {
		Optional<List<EmployeeEntity>> empsOpt = getBean(EmployeeEntityAction.class).loadEmployeesByStore(store, Lists.newArrayList(employeeId));
		Preconditions.checkState(empsOpt.isPresent()&&empsOpt.get().size() == 1, String.format("门店[%s]不存在导购[%s]", store.getId(),employeeId));
		return empsOpt.get().get(0);
	}
	
	/**
	 * 保存简单的会员信息
	 * @param user
	 * @param name
	 * @param sex
	 * @param mobilePhone
	 * @param serviceLevel
	 * @param employeeId
	 */
	public Integer saveSimpleMember(LoginUserContext user, String name, Integer sex, String mobilePhone,
			Integer serviceLevel, Integer employeeId) {
		Objects.requireNonNull(user,"当前登录用户不能为空");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "会员名称[name]不能为空");
		Objects.requireNonNull(sex, "会员性别[sex]不能为空");
		EmployeeEntity employee = null;
		if(null != employeeId) employee = getExistEmployee(user.getExitsStore(), employeeId);
		return getBean(MemberEntityAction.class).addSimpleMember(user, name, sex, mobilePhone, serviceLevel, employee);
	}
	
	/**
	 * 批量分配导购
	 * @param user
	 * @param memberIds
	 * @param employeeId
	 */
	public void allotMembersToEmployee(LoginUserContext user,Collection<Integer> memberIds, Integer employeeId) {
		Objects.requireNonNull(user,"当前登录用户不能为空");
		if(CollectionUtils.isEmpty(memberIds)) return ;
		Objects.requireNonNull(employeeId,"待分配导购不能为空");
		StoreEntity store = user.getExitsStore();
		EmployeeEntity employee = getExistEmployee(store, employeeId);
		getBean(MemberEntityAction.class).allot(store, memberIds, employee);
	}
	 /**
     * 一键分配导购
     * @param storeId 门店ID
     */
    public void oneKeyAllotMembers(Integer storeId) {
    	Objects.requireNonNull(storeId,"门店ID不能为空");
    	StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
    	Optional<List<EmployeeEntity>> empsOpt = getBean(EmployeeEntityAction.class).loadEmployeesByStore(store);
    	if(!empsOpt.isPresent()) return ;
    	if(CollectionUtils.isEmpty(empsOpt.get())) return ;
    	List<EmployeeEntity> enableEmps = Lists.newArrayList();
    	for(EmployeeEntity employee : empsOpt.get()) 
    		if(employee.isEnabled()) enableEmps.add(employee);
    	if(enableEmps.isEmpty()) return ;
    	getBean(MemberEntityAction.class).oneKeyAllotMembers(store, enableEmps);
//    	getBean(MemberEntityAction.class).oneKeyAllotMembers(store, empsOpt.get());
    }
    
    /**
     * 解除一键分配导购
     * @param storeId 门店ID
     */
    public void deallocateOneKeyMembers(Integer storeId) {
    	Objects.requireNonNull(storeId,"门店ID不能为空");
    	StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
    	getBean(MemberEntityAction.class).deallocateMembers(store);
    }
    
    /**
     * 解除导购分配
     * @param employeeId
     */
    public void deallocateMembers(Integer storeId,Integer employeeId) {
    	Objects.requireNonNull(employeeId,"导购ID不能为空");
    	Objects.requireNonNull(storeId,"门店ID不能为空");
    	StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
    	EmployeeEntity employee = getBean(EmployeeEntityAction.class).loadAnyById(store,employeeId);
    	getBean(MemberEntityAction.class).deallocateMembers(store,employee);
    }
    
    /**
     * 迁移导购
     * @param sourceEmpId 原导购Id
     * @param destEmpId 待迁移导购ID
     */
    public void transferMembers(Integer storeId,Integer sourceEmpId,Integer destEmpId) {
    	Objects.requireNonNull(sourceEmpId,"原导购ID不能为空");
    	Objects.requireNonNull(destEmpId,"待迁移导购ID不能为空");
    	Objects.requireNonNull(storeId,"门店ID不能为空");
    	StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
    	EmployeeEntity sourceEmp = getBean(EmployeeEntityAction.class).loadEmployee(store, sourceEmpId);
    	EmployeeEntity destEmp = getBean(EmployeeEntityAction.class).loadEmployee(store, destEmpId);
    	getBean(MemberEntityAction.class).transferMembers(sourceEmp, destEmp);
    }
    
    /**
     * 重新分配会员给导购
     * @param sourceEmpId 原导购ID
     * @param destEmpId 目标导购ID
     * @param memberIds 待分配会员
     */
    public void reassignMembers(Integer storeId,Integer sourceEmpId,Integer destEmpId,List<Integer> memberIds) {
    	Objects.requireNonNull(storeId,"门店ID不能为空");
    	Objects.requireNonNull(sourceEmpId,"原导购ID不能为空");
    	Objects.requireNonNull(destEmpId,"目标导购ID不能为空");
    	if(CollectionUtils.isEmpty(memberIds)) return ;
    	StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
    	EmployeeEntity sourceEmp = getBean(EmployeeEntityAction.class).loadEmployee(store, sourceEmpId);
    	List<MemberEntity> members = getBean(MemberEntityAction.class).loadAllocatedMembers(store, sourceEmp, memberIds);
    	if(CollectionUtils.isEmpty(members)) return ;
    	EmployeeEntity destEmp = getBean(EmployeeEntityAction.class).loadEmployee(store, destEmpId);
    	getBean(MemberEntityAction.class).reassignMembers(sourceEmp, destEmp, members);
    }
    
    /**
     * 指定分配随机会员
     * @param storeId
     * @param assgins
     */
    public void assginRandomMembers(Integer storeId,List<AssignDTO> assigns) {
    	Objects.requireNonNull(storeId);
    	if(CollectionUtils.isEmpty(assigns)) return ;
    	StoreEntity store = getBean(StoreEntityAction.class).loadById(storeId);
    	getBean(MemberEntityAction.class).assignRandomMembers(store, assigns);
    }
}
