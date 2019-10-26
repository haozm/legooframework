package com.legooframework.model.families.service;

import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.EmployeeEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.families.entity.FamilyBO;
import com.legooframework.model.families.entity.FamilyEntity;
import com.legooframework.model.families.entity.FamilyEntityAction;
import com.legooframework.model.families.entity.MemberBO;
import com.legooframework.model.families.entity.MemberFamilyBO;
import com.legooframework.model.families.entity.MemberFamilyEntity;

public class FamilyService extends BaseService {

	private final static Logger logger = LoggerFactory.getLogger(FamilyService.class);

	/**
	 * @param loginUser
	 * @param memberFamilyBO
	 */
	public void saveOrUpdateMemberFamily(LoginUserContext loginUser, MemberFamilyBO memberFamilyBO) {
		Preconditions.checkNotNull(loginUser);
		Preconditions.checkNotNull(memberFamilyBO);
		FamilyBO familyBo = memberFamilyBO.getFamily();
		MemberBO memberBo = memberFamilyBO.getMember();
		Date birthday = null;
		if (null != familyBo.getBirthday()) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			try {
				birthday = format.parse(familyBo.getBirthday());
			} catch (ParseException e) {
				throw new RuntimeException("birthday不符合[yyyy-MM-dd]格式");
			}
		}
		if (null != memberFamilyBO.getFamily().getId()) {
			modifyMemberFamily(loginUser, familyBo.getId(), null == memberBo ? null : memberBo.getId(),
					familyBo.getMembership(), familyBo.getAppellation(), familyBo.getName(), familyBo.getPhone(),
					familyBo.getSex(), familyBo.getCalendarType(), birthday, familyBo.getHeight(), familyBo.getWeight(),
					familyBo.getCareer(), familyBo.getContactable(), familyBo.getEmployeeId());
		} else {
			String familyId = saveMemberFamily(loginUser, null == memberBo ? null : memberBo.getId(),
					familyBo.getMembership(), familyBo.getAppellation(), familyBo.getName(), familyBo.getPhone(),
					familyBo.getSex(), familyBo.getCalendarType(), birthday, familyBo.getHeight(), familyBo.getWeight(),
					familyBo.getCareer(), 1 == familyBo.getContactable(), familyBo.getEmployeeId());
		}
	}
	
	/**
	 * 新增会员家庭成员
	 *
	 * @param loginUser
	 * @param memberId
	 * @param membership
	 * @param appellation
	 * @param name
	 * @param phone
	 * @param sex
	 * @param calendarType
	 * @param birthday
	 * @param height
	 * @param weight
	 * @param career
	 * @param contactable
	 * @param employeeId
	 */
	public String saveMemberFamily(LoginUserContext loginUser, Integer memberId, Integer membership, String appellation,
			String name, String phone, Integer sex, Integer calendarType, Date birthday, String height, String weight,
			Integer career, boolean contactable, Integer employeeId) {
		StoreEntity store = loginUser.getExitsStore();
		MemberEntity member = null;
		if (null != memberId)
			member = getBean(MemberEntityAction.class).loadMemberById(store, memberId);
		EmployeeEntity employee = null;
		if (null != employeeId)
			employee = getBean(EmployeeEntityAction.class).loadById(employeeId);
		if (null != member) {
			return getBean(FamilyEntityAction.class).addMemberFamily(loginUser, member, membership, appellation, name,
					phone, sex, calendarType, birthday, height, weight, career, contactable, employee, store);
		} else {
			return getBean(FamilyEntityAction.class).addFamily(loginUser, name, phone, sex, calendarType, birthday,
					height, weight, career, contactable, employee, store);
		}
	}

	/**
	 * 修改会员家庭成员
	 *
	 * @param membership
	 * @param appellation
	 * @param name
	 * @param phone
	 * @param sex
	 * @param calendarType
	 * @param birthday
	 * @param height
	 * @param weight
	 * @param career
	 * @param contactable
	 */
	public void modifyMemberFamily(LoginUserContext loginUser, String familyId, Integer memberId, Integer membership,
			String appellation, String name, String phone, Integer sex, Integer calendarType, Date birthday,
			String height, String weight, Integer career, Integer contactable, Integer employeeId) {
		StoreEntity store = loginUser.getExitsStore();
		MemberEntity member = null;
		if (null != memberId)
			member = getBean(MemberEntityAction.class).loadMemberById(store, memberId);
		EmployeeEntity employee = null;
		if (null != employeeId)
			employee = getBean(EmployeeEntityAction.class).loadById(employeeId);
		MemberFamilyEntity memberFamily = getBean(FamilyEntityAction.class).loadMemberFamily(familyId);
		if (null != member) {
			Optional<MemberFamilyEntity> memberFamilyOpt = memberFamily.modify(member, membership, appellation);
			if (memberFamilyOpt.isPresent())
				getBean(FamilyEntityAction.class).modifyMemberFamily(memberFamilyOpt.get());
		} else {
			getBean(FamilyEntityAction.class).removeMemberFamily(memberFamily);
		}
		Optional<FamilyEntity> familyOpt = memberFamily.getFamily().modify(name, phone, sex, calendarType, birthday,
				height, weight, career, contactable, employee);
		if (familyOpt.isPresent())
			getBean(FamilyEntityAction.class).modifyFamily(familyOpt.get());
	}
	
	/**
	 * 绑定会员
	 *
	 * @param familyId
	 * @param memberId
	 * @param membership
	 * @param appellation
	 */
	public void bindMember(LoginUserContext loginUser, String familyId, Integer memberId, Integer membership,
			String appellation) {
		StoreEntity store = loginUser.getExitsStore();
		FamilyEntity family = getBean(FamilyEntityAction.class).loadFamilyById(familyId);
		MemberEntity member = getBean(MemberEntityAction.class).loadMemberById(store, memberId);
		Optional<MemberFamilyEntity> memberFamilyOpt = getBean(FamilyEntityAction.class).findMemberFamily(familyId);
		if (memberFamilyOpt.isPresent()) {
			MemberFamilyEntity memberFamily = memberFamilyOpt.get();
			getBean(FamilyEntityAction.class).clearMemberFamily(memberFamily);
		}
		MemberFamilyEntity memberFamily = MemberFamilyEntity.create(member, membership, appellation, family);
		getBean(FamilyEntityAction.class).addMemberFamily(memberFamily);
	}

	@Override
	protected Bundle getLocalBundle() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
