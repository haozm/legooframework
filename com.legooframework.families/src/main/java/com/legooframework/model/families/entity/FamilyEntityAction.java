package com.legooframework.model.families.entity;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class FamilyEntityAction extends BaseEntityAction<FamilyEntity> {

	protected FamilyEntityAction() {
		super("FamilyEntity", null);
	}

	/**
	 * 查找会员家庭成员
	 * 
	 * @param familyId
	 * @return
	 */
	public Optional<FamilyEntity> findFamilyById(String familyId) {
		if (Strings.isNullOrEmpty(getModel()))
			return Optional.absent();
		Map<String, Object> map = Maps.newHashMapWithExpectedSize(1);
		map.put("familyId", familyId);
		String sql = getExecSql("findMemberFamily", map);
		FamilyEntity result = getNamedParameterJdbcTemplate().query(sql, map, new FamilyResultSetExtractorImpl());
		return Optional.fromNullable(result);
	}
	
	/**
	 * 查找多个家庭成员
	 * @param familyIds
	 */
	public Optional<List<FamilyEntity>> findFamilyByIds(Collection<String> familyIds) {
		if(CollectionUtils.isEmpty(familyIds)) Optional.absent();
		Map<String,Object> map = Maps.newHashMap();
		map.put("familyIds", familyIds);
		String sql = getExecSql("find_familys", map);
		List<FamilyEntity> result = getNamedParameterJdbcTemplate().query(sql, map, new RowMapperImpl());
		if(CollectionUtils.isEmpty(result)) return Optional.absent();
		return Optional.of(result);
	}
	/**
	 * 加载会员家庭成员
	 * @param familyId
	 * @return
	 */
	public FamilyEntity loadFamilyById(String familyId) {
		Optional<FamilyEntity> opt = findFamilyById(familyId);
		Preconditions.checkState(opt.isPresent(), "家庭成员不存在");
		return opt.get();
	}

	/**
	 * 查找会员家庭成员
	 * 
	 * @param member
	 * @param familyId
	 * @return
	 */
	public Optional<MemberFamilyEntity> findMemberFamily(MemberEntity member, String familyId) {
		Preconditions.checkNotNull(member, "会员不能为空");
		return findMemberFamily(member.getId(), familyId);
	}

	/**
	 * 查找会员家庭成员
	 * 
	 * @param familyId
	 * @return
	 */
	public Optional<MemberFamilyEntity> findMemberFamily(String familyId) {
		if (Strings.isNullOrEmpty(familyId))
			return Optional.absent();
		Map<String, Object> map = Maps.newHashMap();
		map.put("familyId", familyId);
		String sql = getExecSql("findMemberFamily", map);
		MemberFamilyEntity result = getNamedParameterJdbcTemplate().query(sql, map,
				new MemberFamilyResultSetExtractorImpl());
		return Optional.fromNullable(result);
	}

	/**
	 * 加载会员家庭成员
	 * 
	 * @param familyId
	 * @return
	 */
	public MemberFamilyEntity loadMemberFamily(String familyId) {
		Optional<MemberFamilyEntity> opt = findMemberFamily(familyId);
		Preconditions.checkState(opt.isPresent(), String.format("会员家庭成员【%s】不存在", familyId));
		return opt.get();
	}

	/**
	 * 查找会员家庭成员
	 * 
	 * @param memberId
	 * @param familyId
	 * @return
	 */
	public Optional<MemberFamilyEntity> findMemberFamily(Integer memberId, String familyId) {
		if (null == memberId || Strings.isNullOrEmpty(familyId))
			return Optional.absent();
		Map<String, Object> map = Maps.newHashMap();
		map.put("memberId", memberId);
		map.put("familyId", familyId);
		String sql = getExecSql("findMemberFamily", map);
		MemberFamilyEntity result = getNamedParameterJdbcTemplate().query(sql,
				map,new MemberFamilyResultSetExtractorImpl());
		return Optional.fromNullable(result);
	}

	/**
	 * 加载会员家庭成员
	 * 
	 * @param member
	 * @param familyId
	 * @return
	 */
	public MemberFamilyEntity loadMemberFamily(MemberEntity member, String familyId) {
		Preconditions.checkNotNull(member, "会员不能为空");
		return loadMemberFamily(member.getId(), familyId);
	}

	/**
	 * 加载会员家庭成员
	 * 
	 * @param memberId
	 * @param familyId
	 * @return
	 */
	public MemberFamilyEntity loadMemberFamily(Integer memberId, String familyId) {
		Optional<MemberFamilyEntity> opt = findMemberFamily(memberId, familyId);
		Preconditions.checkState(opt.isPresent(), "会员家庭成员不存在");
		return opt.get();
	}

	/**
	 * 添加会员家庭成员
	 * 
	 * @param member
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
	 * @param employee
	 * @param store
	 */
	public String addMemberFamily(LoginUserContext loginUser, MemberEntity member, Integer membership, String appellation,
			String name, String phone, Integer sex, Integer calendarType, Date birthday, String height, String weight,
			Integer career, boolean contactable, EmployeeEntity employee, StoreEntity store) {
		FamilyEntity family = FamilyEntity.create(name, phone, sex, calendarType, birthday, height, weight, career,
				contactable, employee, store);
		family.setCreator(loginUser);
		addFamily(family);
		if (null == member)
			return family.getId();
		MemberFamilyEntity memberFamily = MemberFamilyEntity.create(member, membership, appellation, family);
		addMemberFamily(memberFamily);
		return family.getId();
	}

	/**
	 * 添加会员家庭成员
	 * 
	 * @param entity
	 */
	public void addMemberFamily(MemberFamilyEntity entity) {
		Preconditions.checkNotNull(entity);
		String sql = getExecSql("add_member_family", null);
		int result = getNamedParameterJdbcTemplate().update(sql, entity.toMap());
		Preconditions.checkArgument(1 == result, "新增会员家庭成员");
	}
	
	public void removeSimpleFamily(String familyId) {
		if(Strings.isNullOrEmpty(familyId)) return ;
		Map<String,Object> map = Maps.newHashMap();
		map.put("familyId", familyId);
		String sql = getExecSql("delete_family", map);
		getNamedParameterJdbcTemplate().update(sql, map);
		getNamedParameterJdbcTemplate().update(getExecSql("delete_member_family", map), map);
	}
	
	/**
	 * 移除家庭成员
	 * @param entity
	 */
	public void removeMemberFamily(MemberFamilyEntity entity) {
		Preconditions.checkNotNull(entity);
		if(null == entity.getMemberId()) return ;
		String sql = getExecSql("remove_member_family", null);
		int result = getNamedParameterJdbcTemplate().update(sql, entity.toMap());
		Preconditions.checkArgument(1 == result, "移除会员家庭成员");
	}
	
	/**
	 * 添加家庭成员
	 * 
	 * @param name
	 * @param phone
	 * @param sex
	 * @param calendarType
	 * @param birthday
	 * @param height
	 * @param weight
	 * @param career
	 * @param contactable
	 * @param employee
	 * @param store
	 */
	public String addFamily(LoginUserContext loginUser, String name, String phone, Integer sex, Integer calendarType,
			Date birthday, String height, String weight, Integer career, boolean contactable, EmployeeEntity employee,
			StoreEntity store) {
		FamilyEntity family = FamilyEntity.create(name, phone, sex, calendarType, birthday, height, weight, career,
				contactable, null == employee?loginUser.getEmployee():employee, store);
		family.setCreator(loginUser);
		addFamily(family);
		return family.getId();
	}
	
	/**
	 * 添加家庭成员
	 * 
	 * @param entity
	 */
	public void addFamily(FamilyEntity entity) {
		Preconditions.checkNotNull(entity);
		String sql = getExecSql("add_family", null);
		int result = getNamedParameterJdbcTemplate().update(sql, entity.toMap());
		Preconditions.checkArgument(1 == result, "新增家庭成员");
	}

	/**
	 * 更新会员家庭成员
	 * 
	 * @param familyId
	 * @param memberOpt
	 * @param membershipOpt
	 * @param appellationOpt
	 * @param nameOpt
	 * @param phoneOpt
	 * @param sexOpt
	 * @param calendarTypeOpt
	 * @param birthdayOpt
	 * @param heightOpt
	 * @param weightOpt
	 * @param careerOpt
	 * @param contactableOpt
	 * @param employeeOpt
	 */
	public void modifyMemberFamily(String familyId, MemberEntity member, Integer membership,
			String appellation, String name, String phone,
			Integer sex, Integer calendarType, Date birthday,
			String height, String weight, Integer career,
			Integer contactable, EmployeeEntity employee) {
		MemberFamilyEntity memberFamily = loadMemberFamily(familyId);
		Optional<MemberFamilyEntity> memberFamilyModifyOpt = memberFamily.modify(member, membership,
				appellation);
		if (memberFamilyModifyOpt.isPresent())
			modifyMemberFamily(memberFamilyModifyOpt.get());
		Optional<FamilyEntity> familyModifyOpt = memberFamily.getFamily().modify(name, phone, sex,
				calendarType, birthday, height, weight, career, contactable, employee);
		if (familyModifyOpt.isPresent())
			modifyFamily(familyModifyOpt.get());
	}

	/**
	 * 更新会员家庭成员信息
	 * 
	 * @param memberFamily
	 */
	public void modifyMemberFamily(MemberFamilyEntity memberFamily) {
		Preconditions.checkNotNull(memberFamily);
		String sql = getExecSql("update_member_family", null);
		int result = getNamedParameterJdbcTemplate().update(sql, memberFamily.toMap());
		Preconditions.checkState(1 == result, "更新会员家庭成员失败");
	}

	/**
	 * 更新家庭成员信息
	 * 
	 * @param family
	 */
	public void modifyFamily(FamilyEntity family) {
		Preconditions.checkNotNull(family);
		String sql = getExecSql("update_family", null);
		int result = getNamedParameterJdbcTemplate().update(sql, family.toMap());
		Preconditions.checkState(1 == result, "更新家庭成员失败");
	}
	
	public void clearMemberFamily(MemberFamilyEntity memberFamily) {
		Preconditions.checkNotNull(memberFamily, "memberFamily不能为空");
		Map<String,Object> map = Maps.newHashMap();
		map.put("memberId", memberFamily.getMemberId());
		map.put("familyId", memberFamily.getFamily().getId());
		String sql =getExecSql("clear_member_family", null);
		getNamedParameterJdbcTemplate().update(sql, map);
	}

	/**
	 * 加载会员家庭成员信息
	 * @param familyId
	 * @return
	 */
	public MemberFamilyBO loadMemberFamilyBO(String familyId) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(familyId),String.format("家庭成员【%s】不存在", familyId));
		Map<String, Object> map = Maps.newHashMap();
		map.put("familyId", familyId);
		String memberSql = getExecSql("find_member_bo", map);
		MemberBO memberBO = null;
		try {
			memberBO = getNamedParameterJdbcTemplate().queryForObject(memberSql, map, new BeanPropertyRowMapper<MemberBO>(MemberBO.class));
		}catch(EmptyResultDataAccessException e) {
		}
		String familySql = getExecSql("find_family_bo", map);
		FamilyBO familyBO = null;
		try {
			familyBO  = getNamedParameterJdbcTemplate().queryForObject(familySql, map, new BeanPropertyRowMapper<FamilyBO>(FamilyBO.class));
		}catch(EmptyResultDataAccessException e) {
			throw new RuntimeException(String.format("家庭成员【%s】不存在", familyId));
		}
		MemberFamilyBO memberFamilyBO = new MemberFamilyBO();
		memberFamilyBO.setMember(memberBO);
		memberFamilyBO.setFamily(familyBO);
		return memberFamilyBO;
		
		
	}
	@Override
	protected ResultSetExtractor<FamilyEntity> getResultSetExtractor() {
		// TODO Auto-generated method stub
		return null;
	}

	private class MemberFamilyResultSetExtractorImpl implements ResultSetExtractor<MemberFamilyEntity> {
		@Override
		public MemberFamilyEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
			while (rs.next())
				return buildMemberFamilyExtractor(rs);
			return null;
		}
	}

	private class FamilyResultSetExtractorImpl implements ResultSetExtractor<FamilyEntity> {
		@Override
		public FamilyEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
			while (rs.next())
				return buildFamilyExtractor(rs);
			return null;
		}
	}
	

	class RowMapperImpl implements RowMapper<FamilyEntity> {
		@Override
		public FamilyEntity mapRow(ResultSet resultSet, int i) throws SQLException {
			return buildFamilyExtractor(resultSet);
		}
	}

	public FamilyEntity buildFamilyExtractor(ResultSet resultSet) {
		try {
			String id = resultSet.getString("id");
			String name = resultSet.getString("name");
			String phone = resultSet.getString("phone");
			Integer sex = (Integer) resultSet.getObject("sex");
			Integer calendarType = null == resultSet.getObject("calendarType") ? null
					: (Integer) resultSet.getObject("calendarType");
			Date birthday = resultSet.getDate("birthday");
			String height = resultSet.getString("height");
			String weight = resultSet.getString("weight");
			Integer career = null == resultSet.getObject("career") ? null : (Integer) resultSet.getObject("career");
			Integer contactable = null == resultSet.getObject("contactable") ? null
					: (Integer) resultSet.getObject("contactable");
			Integer employeeId = null == resultSet.getObject("employeeId") ? null : (Integer) resultSet.getObject("employeeId");
			Integer storeId = null == resultSet.getObject("storeId") ? null : (Integer) resultSet.getObject("storeId");
			Integer companyId = null == resultSet.getObject("companyId") ? null
					: Integer.parseInt(resultSet.getString("companyId")) ;
			return FamilyEntity.valueOf(id, name, phone, sex, calendarType, birthday, height, weight, career,
					contactable, employeeId, storeId, companyId);
		} catch (SQLException e) {
			throw new RuntimeException("数据库中获取家庭成员数据异常", e);
		}
	}

	public MemberFamilyEntity buildMemberFamilyExtractor(ResultSet resultSet) {
		FamilyEntity family = buildFamilyExtractor(resultSet);
		try {
			Integer memberFamilyId = resultSet.getInt("memberFamilyId");
			Integer memberId = null == resultSet.getObject("memberId") ? null:(Integer)resultSet.getObject("memberId");
			Integer membership = null == resultSet.getObject("membership") ? null:(Integer)resultSet.getObject("membership");
			String appellation = resultSet.getString("appellation");
			return MemberFamilyEntity.valueOf(memberFamilyId, memberId, family, membership, appellation);
		} catch (SQLException e) {
			throw new RuntimeException("数据库中获取会员家庭成员数据异常", e);
		}
	}

}
