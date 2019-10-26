package com.csosm.module.member.entity;

import java.math.BigDecimal;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.commons.entity.ResultSetUtil;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MemberEntityAction extends BaseEntityAction<MemberEntity> {
	
	private static final Logger logger = LoggerFactory.getLogger(MemberEntityAction.class);
	
	protected MemberEntityAction() {
		super("MemberEntity", "adapterCache");
		// TODO Auto-generated constructor stub
	}

	private boolean existMobilePhone(String mobilePhone) {
		Map<String, Object> map = Maps.newHashMap();
		map.put("mobilePhone", mobilePhone);
		String sql = getExecSql("count_mobilePhone");
		Integer count = getNamedParameterJdbcTemplate().queryForObject(sql, map, Integer.class);
		return count > 0;
	}

	/**
	 * 查询会员，不包含会员消费行为、会员附加信息、会员卡信息
	 * 
	 * @param memberId
	 * @return
	 */
	public List<MemberEntity> findMembersByIds(StoreEntity store, Collection<Integer> memberIds) {
		Optional<List<MemberEntity>> membersOpt = findMembersByIds(store,memberIds,false,false);
		if(!membersOpt.isPresent()) return Collections.EMPTY_LIST;
		return membersOpt.get();
	}
	
	/**
	 * 按条件查询会员列表
	 * @param store 门店
	 * @param memberIds 会员id集合
	 * @param needPhone 是否需要电话号码
	 * @param effective 是否启用
	 * @return
	 */
	public Optional<List<MemberEntity>> findMembersByIds(StoreEntity store, Collection<Integer> memberIds,boolean needPhone,
			boolean effective) {
		return findMembersByIds(store, memberIds, needPhone, effective,null);
	}
	
	private Optional<List<MemberEntity>> findMembersByIds(StoreEntity store, Collection<Integer> memberIds,boolean needPhone,
			boolean effective,String mobilePhone) {
		Objects.requireNonNull(store, "入参store不能为空");
		if (CollectionUtils.isEmpty(memberIds))
			return Optional.empty();
		Map<String, Object> params = Maps.newHashMap();
		Preconditions.checkState(store.getCompanyId().isPresent(), String.format("门店[%s]无公司信息", store.getId()));
		params.put("companyId", store.getCompanyId().get());
		params.put("storeId", store.getId());
		if(null != memberIds)
			params.put("memberIds", memberIds);
		params.put("needPhone", needPhone);
		params.put("effective", effective);
		if(null != mobilePhone) {
			params.put("mobilePhone", mobilePhone);
		}
		String sql = getExecSql("select_members", params);
		List<MemberEntity> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapperImpl());
		return CollectionUtils.isEmpty(result)?Optional.empty():Optional.of(result);
	}
	
	/**
	 * 根据电话号码查询会员信息
	 * @param store
	 * @param mobileNum 电话号码
	 * @return
	 */
	public Optional<MemberEntity> findByStoreWithMobile(StoreEntity store, String mobileNum) {
		Optional<List<MemberEntity>> membersOpt = findMembersByIds(store, null, false, false,mobileNum);
		if (!membersOpt.isPresent() || CollectionUtils.isEmpty(membersOpt.get()) || membersOpt.get().size() != 1)
			return Optional.empty();
		return Optional.of(membersOpt.get().get(0));
	}
	
	/**
	 * 查询完整的会员
	 * 
	 * @param memberId
	 * @return
	 */
	public Optional<MemberEntity> findMemberById(StoreEntity store, Integer memberId) {
		List<MemberEntity> members = findMembersByIds(store, Lists.newArrayList(memberId));
		if (CollectionUtils.isEmpty(members) || members.size() != 1)
			return Optional.empty();
		return Optional.of(members.get(0));
	}
	
	

	/**
	 * 加载完整的会员
	 * 
	 * @param memberId
	 * @return
	 */
	public MemberEntity loadMemberById(StoreEntity store, Integer memberId) {
		Objects.requireNonNull(store);
		Optional<MemberEntity> memberOpt = findMemberById(store, memberId);
		Preconditions.checkState(memberOpt.isPresent(), String.format("会员[%s]不存在", memberId));
		return memberOpt.get();
	}

	/**
	 * 通过会员查找会员扩展信息
	 * 
	 * @param member
	 * @return
	 */
	public Optional<MemberExtraEntity> findByMember(MemberEntity member) {
		Objects.requireNonNull(member);
		Map<String, Object> map = Maps.newHashMap();
		map.put("memberId", member.getId());
		String sql = getExecSql("findMemberExtraById", null);
		MemberExtraEntity entity = getNamedParameterJdbcTemplate().query(sql, map, getMemberExtraResultSetExtractor());
		if (entity == null)
			return Optional.empty();
		return Optional.of(entity);
	}

	/**
	 * 通过会员加载会员扩展信息 如无数据，已返回异常
	 * 
	 * @param member
	 * @return
	 */
	public MemberExtraEntity loadByMember(MemberEntity member) {
		Objects.requireNonNull(member);
		Optional<MemberExtraEntity> opt = findByMember(member);
		Preconditions.checkState(opt.isPresent(), String.format("会员[%s]扩展信息不存在", member.getId()));
		return opt.get();
	}

	/**
	 * 获取门店未分配会员
	 * 
	 * @param store
	 *            门店
	 * @return
	 */
	public List<MemberEntity> loadUnallocatedMembers(StoreEntity store) {
		Objects.requireNonNull(store, "门店不能为空");
		Preconditions.checkArgument(store.getCompanyId().isPresent(), String.format("门店无公司信息", store.getId()));
		Map<String, Object> map = Maps.newHashMap();
		map.put("storeId", store.getId());
		map.put("companyId", store.getCompanyId().get());
		String sql = getExecSql("load_unallocate_members", map);
		List<MemberEntity> result = getNamedParameterJdbcTemplate().query(sql, map, new RowMapperImpl());
		if (CollectionUtils.isEmpty(result))
			return Collections.EMPTY_LIST;
		return result;
	}

	/**
	 * @param employee导购
	 * @param memberIds
	 *            会员ID集合
	 * @return
	 */
	public List<MemberEntity> loadAllocatedMembers(StoreEntity store, EmployeeEntity employee,
			List<Integer> memberIds) {
		Objects.requireNonNull(employee, "职员[导购] 不能为空");
		Objects.requireNonNull(store, "门店不能为空");
		Preconditions.checkArgument(employee.getStoreId().isPresent(), String.format("导购[%s]无门店信息", employee.getId()));
		Preconditions.checkArgument(store.getCompanyId().isPresent(), String.format("门店无公司信息", store.getId()));
		Preconditions.checkArgument(store.getId().intValue() == employee.getStoreId().get().intValue(),
				String.format("导购[%s]非门店[%s]职员", employee.getId(), store.getId()));
		if (CollectionUtils.isEmpty(memberIds))
			return Collections.EMPTY_LIST;
		Map<String, Object> params = Maps.newHashMap();
		params.put("employeeId", employee.getId());
		params.put("memberIds", memberIds);
		params.put("storeId", store.getId());
		params.put("companyId", store.getCompanyId().get());
		String sql = getExecSql("load_members_4Emp", params);
		List<MemberEntity> result = getNamedParameterJdbcTemplate().query(sql, params, new RowMapperImpl());
		if (CollectionUtils.isEmpty(result))
			return Collections.EMPTY_LIST;
		return result;
	}

	/**
	 * 获取会员ID最大值并生成会员ID
	 * 
	 * @return
	 */
	private Integer generateMemberId() {
		String sql = getExecSql("select_member_maxId");
		Integer maxId = getNamedParameterJdbcTemplate().queryForObject(sql, Maps.newHashMap(), Integer.class);
		return ++maxId;
	}
	/**
	 * 新增会员
	 * 
	 * @param name
	 * @param sex
	 * @param mobilePhone
	 * @param serviceLevel
	 * @param employee
	 */
	public Integer addSimpleMember(LoginUserContext user, String name, Integer sex, String mobilePhone,
			Integer serviceLevel, EmployeeEntity employee) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "会员名称不能为空");
		Objects.requireNonNull(sex, "会员性别不能为空");
		Preconditions.checkState(user.getStore().isPresent(), String.format("当前登录用户[%s]无门店信息", user.getUserId()));
		if (null != mobilePhone)
			Preconditions.checkArgument(!existMobilePhone(mobilePhone), String.format("电话【%s】已存在", mobilePhone));
		MemberEntity member = MemberEntity.createSimpleMember(generateMemberId(), name, sex, mobilePhone, serviceLevel,
				employee, user.getStore().get());
		member.setCreator(user);
		addSimpleMember(member);
		addSimpleMemberAddition(member.createSimpleMemberAddition());
		addSimpleMemberCard(member.createSimpleMemberCard());
		addSimpleMemberConsume(member.createSimpleMemberConsume());
		addSimpleMemberExtra(member.createSimpleMemberExtra());
		return member.getId();
	}

	/**
	 * 完整新建会员信息
	 * 
	 * @param name
	 * @param memberType
	 * @param sex
	 * @param serviceLevel
	 * @param phone
	 * @param memberCardType
	 * @param memberCardNum
	 * @param createCardTime
	 * @param createCardStoreId
	 * @param createCardStoreName
	 * @param weixinId
	 * @param qqNum
	 * @param weiboNum
	 * @param email
	 * @param gregorianBirthday
	 * @param lunarBirthday
	 * @param calendarType
	 * @param iconUrl
	 * @param marryStatus
	 * @param detailAddress
	 * @param idols
	 * @param carePeople
	 * @param zodiac
	 * @param characterType
	 * @param jobType
	 * @param faithType
	 * @param likeContact
	 * @param store
	 */
	public Integer addMember(LoginUserContext user, String name, Integer memberType, String namePinyin, Integer sex,
			Integer serviceLevel, String phone, String telePhone, String certificate, Integer certificateType,
			Integer memberCardType, String memberCardNum, Date createCardTime, Integer createCardStoreId,
			String createCardStoreName, String weixinId, String qqNum, String weiboNum, String email,
			Date gregorianBirthday, Date lunarBirthday, Integer calendarType, String iconUrl, Integer marryStatus,
			String detailAddress, String idols, String carePeople, Integer zodiac, Integer characterType,
			String jobType, Integer faithType, String hobby, String likeBrand, Integer likeContact, String specialDay,
			Integer education, Integer likeContactTime, StoreEntity store) {
		Objects.requireNonNull(store, "入参门店不能为空");
		Objects.requireNonNull(user, "当前登录用户不能为空");
		if (null != phone)
			Preconditions.checkArgument(!existMobilePhone(phone), String.format("电话【%s】已存在", phone));
		MemberEntity member = MemberEntity.createMember(generateMemberId(), name, memberType, namePinyin, sex,
				serviceLevel, phone, telePhone, certificate, certificateType, memberCardType, memberCardNum,
				createCardTime, createCardStoreId, createCardStoreName, weixinId, qqNum, weiboNum, email,
				gregorianBirthday, lunarBirthday, calendarType, iconUrl, marryStatus, detailAddress, idols, carePeople,
				zodiac, characterType, jobType, faithType, hobby, likeBrand, likeContact, specialDay, education,
				likeContactTime, store);
		member.setCreator(user);
		addMember(member);
		addMemberAddition(member.getMemberAddition());
		addMemberCard(member.getMemberCard());
		addSimpleMemberConsume(member.createSimpleMemberConsume());
		addSimpleMemberExtra(member.createSimpleMemberExtra());
		return member.getId();
	}

	/**
	 * 新增会员基本信息
	 * 
	 * @param member
	 */
	public Integer addSimpleMember(MemberEntity member) {
		Objects.requireNonNull(member);
		String sql = getExecSql("insert_simple_member");
		Integer result = getNamedParameterJdbcTemplate().update(sql, member.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员[%s]基础信息失败", member.getId());
		return member.getId();
	}

	/**
	 * 新增会员基本信息
	 * 
	 * @param member
	 */
	private Integer addMember(MemberEntity member) {
		Objects.requireNonNull(member);
		String sql = getExecSql("insert_member");
		Integer result = getNamedParameterJdbcTemplate().update(sql, member.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员[%s]基础信息失败", member.getId());
		return member.getId();
	}

	/**
	 * 新增会员卡
	 * 
	 * @param memberCard
	 */
	public Integer addSimpleMemberCard(MemberCardEntity memberCard) {
		Objects.requireNonNull(memberCard);
		String sql = getExecSql("update_simple_memberCard");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberCard.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员卡[%s]失败", memberCard.getId());
		return memberCard.getId();
	}

	/**
	 * 新增会员卡
	 * 
	 * @param memberCard
	 */
	public Integer addMemberCard(MemberCardEntity memberCard) {
		Objects.requireNonNull(memberCard);
		String sql = getExecSql("update_memberCard");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberCard.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员卡[%s]失败", memberCard.getId());
		return memberCard.getId();
	}

	/**
	 * 新增会员消费信息
	 * 
	 * @param memberConsume
	 */
	public Integer addSimpleMemberConsume(MemberConsumeEntity memberConsume) {
		Objects.requireNonNull(memberConsume);
		String sql = getExecSql("insert_simple_memberConsume");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberConsume.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员消费信息[%s]失败", memberConsume.getId());
		return memberConsume.getId();
	}

	/**
	 * 新增会员附加信息
	 * 
	 * @param memberAddition
	 */
	public Integer addSimpleMemberAddition(MemberAdditionEntity memberAddition) {
		Objects.requireNonNull(memberAddition);
		String sql = getExecSql("insert_simple_memberAddition");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberAddition.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员附加信息[%s]失败", memberAddition.getId());
		return memberAddition.getId();
	}

	/**
	 * 新增会员附加信息
	 * 
	 * @param memberAddition
	 */
	public Integer addMemberAddition(MemberAdditionEntity memberAddition) {
		Objects.requireNonNull(memberAddition);
		String sql = getExecSql("insert_memberAddition");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberAddition.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员附加信息[%s]失败", memberAddition.getId());
		return memberAddition.getId();
	}

	/**
	 * 新增会员扩展信息
	 * 
	 * @param memberExtra
	 */
	public Integer addSimpleMemberExtra(MemberExtraEntity memberExtra) {
		Objects.requireNonNull(memberExtra);
		String sql = getExecSql("insert_simple_memberExtra");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberExtra.toStorageMap());
		Preconditions.checkState(result == 1, "新增会员简单的扩展信息[%s]失败", memberExtra.getId());
		return memberExtra.getId();
	}

	/**
	 * 添加会员扩展信息
	 * 
	 * @param store
	 * @param memberId
	 * @param jacketSize
	 * @param bottomsSize
	 * @param braSize
	 * @param briefsSize
	 * @param shoeSize
	 * @param chest
	 * @param clothingLong
	 * @param sleeveLength
	 * @param shoulder
	 * @param waistline
	 * @param hipline
	 * @param thighCircumference
	 * @param kneeCircumference
	 * @param trouserLeg
	 * @param beforeFork
	 * @param afterFork
	 * @param outseam
	 * @param onChest
	 * @param underChest
	 * @param footLength
	 */
	public Integer addMemberExtra(StoreEntity store, Integer memberId, String jacketSize, String bottomsSize,
			String braSize, String briefsSize, String shoeSize, BigDecimal chest, BigDecimal clothingLong,
			BigDecimal sleeveLength, BigDecimal shoulder, BigDecimal waistline, BigDecimal hipline,
			BigDecimal thighCircumference, BigDecimal kneeCircumference, BigDecimal trouserLeg, BigDecimal beforeFork,
			BigDecimal afterFork, BigDecimal outseam, BigDecimal onChest, BigDecimal underChest,
			BigDecimal footLength) {
		MemberEntity member = loadMemberById(store, memberId);
		MemberExtraEntity memberExtra = new MemberExtraEntity(member, jacketSize, bottomsSize, braSize, briefsSize,
				shoeSize, chest, clothingLong, sleeveLength, shoulder, waistline, hipline, thighCircumference,
				kneeCircumference, trouserLeg, beforeFork, afterFork, outseam, onChest, underChest, footLength, null);
		String sql = getExecSql("insertMemberExtra", null);
		getNamedParameterJdbcTemplate().update(sql, memberExtra.toMap());
		if (getCache().isPresent())
			getCache().get().cleanUp();
		return memberExtra.getId();
	}

	/**
	 * 编辑会员信息
	 * 
	 * @param store
	 * @param memberId
	 * @param name
	 * @param memberType
	 * @param sex
	 * @param serviceLevel
	 * @param phone
	 * @param memberCardType
	 * @param memberCardNum
	 * @param createCardTime
	 * @param qqNum
	 * @param weixinId
	 * @param weiboNum
	 * @param email
	 * @param marryStatus
	 * @param detailAddress
	 * @param idols
	 * @param carePeople
	 * @param zodiac
	 * @param characterType
	 * @param jobType
	 * @param faithType
	 * @param likeContact
	 * @param gregorianBirthday
	 * @param lunarBirthday
	 * @param iconUrl
	 * @param calendarType
	 */
	public Integer editMember(LoginUserContext user, Integer memberId, String iconUrl, String name, String namePinyin,
			Integer sex, Date birthday, Date lunarBirthday, Integer calendarType, Integer memberType,
			Integer serviceLevel, String email, String mobilephone, String telephone, String qqNum, String weiboNum,
			String weixinId, Integer marryStatus, String idols, String carePeople, Integer zodiac,
			Integer characterType, String jobType, Integer faithType, String hobby, String likeBrand,
			Integer likeContact, String specialDay, Integer education, Integer likeContactTime, String certificate,
			Integer certificateType, String detailAddress, Integer memberCardType, String memberCardNum) {
		MemberEntity member = loadMemberById(user.getExitsStore(), memberId);
		MemberEntity memberBase = member.modify(iconUrl, name, namePinyin, sex, birthday, lunarBirthday, calendarType,
				memberType, serviceLevel, email, mobilephone, telephone, qqNum, weiboNum, weixinId);
		memberBase.setModifyUser(user);
		if (!memberBase.equalsBaseInfo(member))
			updateMemberBase(memberBase);
		MemberAdditionEntity memberAddition = member.getMemberAddition().modify(marryStatus, idols, carePeople, zodiac,
				characterType, jobType, faithType, hobby, likeBrand, likeContact, specialDay, education,
				likeContactTime, certificate, certificateType, detailAddress);
		if (!memberAddition.equals(member.getMemberAddition()))
			updateMemberAddition(memberAddition);
		MemberCardEntity memberCard = member.getMemberCard().modify(memberCardType, memberCardNum);
		if (!memberCard.equals(member.getMemberCard()))
			updateMemberCard(memberCard);
		return memberCard.getId();
	}

	/**
	 * 修改会员基本信息
	 * 
	 * @param user
	 * @param memberId
	 * @param iconUrl
	 * @param name
	 * @param namePinyin
	 * @param sex
	 * @param birthday
	 * @param lunarBirthday
	 * @param calendarType
	 * @param memberType
	 * @param serviceLevel
	 * @param email
	 * @param mobilephone
	 * @param telephone
	 * @param qqNum
	 * @param weiboNum
	 * @param weixinId
	 */
	public Integer editMemberBase(LoginUserContext user, Integer memberId, String iconUrl, String name,
			String namePinyin, Integer sex, Date birthday, Date lunarBirthday, Integer calendarType, Integer memberType,
			Integer serviceLevel, String email, String mobilephone, String telephone, String qqNum, String weiboNum,
			String weixinId) {
		MemberEntity member = loadMemberById(user.getExitsStore(), memberId);
		MemberEntity memberBase = member.modify(iconUrl, name, namePinyin, sex, birthday, lunarBirthday, calendarType,
				memberType, serviceLevel, email, mobilephone, telephone, qqNum, weiboNum, weixinId);
		memberBase.setModifyUser(user);
		if (!memberBase.equalsBaseInfo(member))
			updateMemberBase(memberBase);
		return memberBase.getId();
	}

	/**
	 * 
	 * @param user
	 * @param memberId
	 * @param iconUrlOpt
	 * @param name
	 * @param namePinyinOpt
	 * @param sex
	 * @param birthdayOpt
	 * @param lunarBirthdayOpt
	 * @param calendarTypeOpt
	 * @param memberTypeOpt
	 * @param serviceLevelOpt
	 * @param emailOpt
	 * @param mobilephoneOpt
	 * @param telephoneOpt
	 * @param qqNumOpt
	 * @param weiboNumOpt
	 * @param weixinIdOpt
	 */
	public Integer editMemberBase(LoginUserContext user, Integer memberId, String name, Integer sex,
			Optional<String> iconUrlOpt, Optional<String> namePinyinOpt, Optional<Date> birthdayOpt,
			Optional<Date> lunarBirthdayOpt, Optional<Integer> calendarTypeOpt, Optional<Integer> memberTypeOpt,
			Optional<Integer> serviceLevelOpt, Optional<String> emailOpt, Optional<String> mobilephoneOpt,
			Optional<String> telephoneOpt, Optional<String> qqNumOpt, Optional<String> weiboNumOpt,
			Optional<String> weixinIdOpt) {
		MemberEntity member = loadMemberById(user.getExitsStore(), memberId);
		MemberEntity memberBase = member.modify(iconUrlOpt, name, namePinyinOpt, sex, birthdayOpt, lunarBirthdayOpt,
				calendarTypeOpt, memberTypeOpt, serviceLevelOpt, emailOpt, mobilephoneOpt, telephoneOpt, qqNumOpt,
				weiboNumOpt, weixinIdOpt);
		memberBase.setModifyUser(user);
		if (!memberBase.equalsBaseInfo(member))
			updateMemberBase(memberBase);
		return memberBase.getId();
	}

	/**
	 * 修改会员附加信息
	 * 
	 * @param store
	 * @param memberId
	 * @param marryStatus
	 * @param idols
	 * @param carePeople
	 * @param zodiac
	 * @param characterType
	 * @param jobType
	 * @param faithType
	 * @param hobby
	 * @param likeBrand
	 * @param likeContact
	 * @param specialDay
	 * @param education
	 * @param likeContactTime
	 * @param certificate
	 * @param certificateType
	 * @param detailAddress
	 */
	public Integer editMemberAddition(StoreEntity store, Integer memberId, Integer marryStatus, String idols,
			String carePeople, Integer zodiac, Integer characterType, String jobType, Integer faithType, String hobby,
			String likeBrand, Integer likeContact, String specialDay, Integer education, Integer likeContactTime,
			String certificate, Integer certificateType, String detailAddress) {
		MemberEntity member = loadMemberById(store, memberId);
		MemberAdditionEntity memberAddition = member.getMemberAddition().modify(marryStatus, idols, carePeople, zodiac,
				characterType, jobType, faithType, hobby, likeBrand, likeContact, specialDay, education,
				likeContactTime, certificate, certificateType, detailAddress);
		if (!memberAddition.equals(member.getMemberAddition()))
			updateMemberAddition(memberAddition);
		return memberAddition.getId();
	}

	/**
	 * @param store
	 * @param memberId
	 * @param marryStatusOpt
	 * @param idolsOpt
	 * @param carePeopleOpt
	 * @param zodiacOpt
	 * @param characterTypeOpt
	 * @param jobTypeOpt
	 * @param faithTypeOpt
	 * @param hobbyOpt
	 * @param likeBrandOpt
	 * @param likeContactOpt
	 * @param specialDayOpt
	 * @param educationOpt
	 * @param likeContactTimeOpt
	 * @param certificateOpt
	 * @param certificateTypeOpt
	 * @param detailAddressOpt
	 */
	public Integer editMemberAddition(StoreEntity store, Integer memberId, Optional<Integer> marryStatusOpt,
			Optional<String> idolsOpt, Optional<String> carePeopleOpt, Optional<Integer> zodiacOpt,
			Optional<Integer> characterTypeOpt, Optional<String> jobTypeOpt, Optional<Integer> faithTypeOpt,
			Optional<String> hobbyOpt, Optional<String> likeBrandOpt, Optional<Integer> likeContactOpt,
			Optional<String> specialDayOpt, Optional<Integer> educationOpt, Optional<Integer> likeContactTimeOpt,
			Optional<String> certificateOpt, Optional<Integer> certificateTypeOpt, Optional<String> detailAddressOpt) {
		MemberEntity member = loadMemberById(store, memberId);
		MemberAdditionEntity memberAddition = member.getMemberAddition().modify(marryStatusOpt, idolsOpt, carePeopleOpt,
				zodiacOpt, characterTypeOpt, jobTypeOpt, faithTypeOpt, hobbyOpt, likeBrandOpt, likeContactOpt,
				specialDayOpt, educationOpt, likeContactTimeOpt, certificateOpt, certificateTypeOpt, detailAddressOpt);
		if (!memberAddition.equals(member.getMemberAddition()))
			updateMemberAddition(memberAddition);
		return memberAddition.getId();
	}

	/**
	 * 修改会员卡信息
	 * 
	 * @param store
	 * @param memberId
	 * @param memberCardType
	 * @param memberCardNum
	 * @param createCardTime
	 */
	public Integer editMemberCard(StoreEntity store, Integer memberId, Integer memberCardType, String memberCardNum) {
		MemberEntity member = loadMemberById(store, memberId);
		MemberCardEntity memberCard = member.getMemberCard().modify(memberCardType, memberCardNum);
		if (!memberCard.equals(member.getMemberCard()))
			updateMemberCard(memberCard);
		return memberCard.getId();
	}

	/**
	 * 修改会员卡信息
	 * 
	 * @param store
	 * @param memberId
	 * @param memberCardTypeOpt
	 * @param memberCardNumOpt
	 */
	public Integer editMemberCard(StoreEntity store, Integer memberId, Optional<Integer> memberCardTypeOpt,
			Optional<String> memberCardNumOpt,Optional<Date> createCardTimeOpt) {
		MemberEntity member = loadMemberById(store, memberId);
		MemberCardEntity memberCard = member.getMemberCard().modify(memberCardTypeOpt, memberCardNumOpt,createCardTimeOpt);
		if (!memberCard.equals(member.getMemberCard()))
			updateMemberCard(memberCard);
		return memberCard.getId();
	}

	/**
	 * 修改会员扩展信息
	 * 
	 * @param store
	 * @param memberId
	 * @param jacketSize
	 * @param bottomsSize
	 * @param braSize
	 * @param briefsSize
	 * @param shoeSize
	 * @param chest
	 * @param clothingLong
	 * @param sleeveLength
	 * @param shoulder
	 * @param waistline
	 * @param hipline
	 * @param thighCircumference
	 * @param kneeCircumference
	 * @param trouserLeg
	 * @param beforeFork
	 * @param afterFork
	 * @param outseam
	 * @param onChest
	 * @param underChest
	 * @param footLength
	 */
	public Integer editMemberExtra(StoreEntity store, Integer memberId, String jacketSize, String bottomsSize,
			String braSize, String briefsSize, String shoeSize, BigDecimal chest, BigDecimal clothingLong,
			BigDecimal sleeveLength, BigDecimal shoulder, BigDecimal waistline, BigDecimal hipline,
			BigDecimal thighCircumference, BigDecimal kneeCircumference, BigDecimal trouserLeg, BigDecimal beforeFork,
			BigDecimal afterFork, BigDecimal outseam, BigDecimal onChest, BigDecimal underChest,
			BigDecimal footLength) {
		MemberEntity member = loadMemberById(store, memberId);
		MemberExtraEntity memberExtra = loadByMember(member);
		MemberExtraEntity clone = memberExtra.modify(jacketSize, bottomsSize, braSize, briefsSize, shoeSize, chest,
				clothingLong, sleeveLength, shoulder, waistline, hipline, thighCircumference, kneeCircumference,
				trouserLeg, beforeFork, afterFork, outseam, onChest, underChest, footLength);
		if (!memberExtra.equals(clone))
			updateMemberExtra(clone);
		return clone.getId();
	}

	/**
	 * 修改会员信息，可修改单个也可修改多个，不需要修改的，请输入 Optional.empty();
	 * 
	 * @param store
	 * @param memberId
	 * @param jacketSizeOpt
	 * @param bottomsSizeOpt
	 * @param braSizeOpt
	 * @param briefsSizeOpt
	 * @param shoeSizeOpt
	 * @param chestOpt
	 * @param clothingLongOpt
	 * @param sleeveLengthOpt
	 * @param shoulderOpt
	 * @param waistlineOpt
	 * @param hiplineOpt
	 * @param thighCircumferenceOpt
	 * @param kneeCircumferenceOpt
	 * @param trouserLegOpt
	 * @param beforeForkOpt
	 * @param afterForkOpt
	 * @param outseamOpt
	 * @param onChestOpt
	 * @param underChestOpt
	 * @param footLengthOpt
	 */
	public Integer editMemberExtra(StoreEntity store, Integer memberId, Optional<String> jacketSizeOpt,
			Optional<String> bottomsSizeOpt, Optional<String> braSizeOpt, Optional<String> briefsSizeOpt,
			Optional<String> shoeSizeOpt, Optional<BigDecimal> chestOpt, Optional<BigDecimal> clothingLongOpt,
			Optional<BigDecimal> sleeveLengthOpt, Optional<BigDecimal> shoulderOpt, Optional<BigDecimal> waistlineOpt,
			Optional<BigDecimal> hiplineOpt, Optional<BigDecimal> thighCircumferenceOpt,
			Optional<BigDecimal> kneeCircumferenceOpt, Optional<BigDecimal> trouserLegOpt,
			Optional<BigDecimal> beforeForkOpt, Optional<BigDecimal> afterForkOpt, Optional<BigDecimal> outseamOpt,
			Optional<BigDecimal> onChestOpt, Optional<BigDecimal> underChestOpt, Optional<BigDecimal> footLengthOpt) {
		MemberEntity member = loadMemberById(store, memberId);
		MemberExtraEntity memberExtra = loadByMember(member);
		MemberExtraEntity clone = memberExtra.modify(jacketSizeOpt, bottomsSizeOpt, braSizeOpt, briefsSizeOpt,
				shoeSizeOpt, chestOpt, clothingLongOpt, sleeveLengthOpt, shoulderOpt, waistlineOpt, hiplineOpt,
				thighCircumferenceOpt, kneeCircumferenceOpt, trouserLegOpt, beforeForkOpt, afterForkOpt, outseamOpt,
				onChestOpt, underChestOpt, footLengthOpt);
		if (!memberExtra.equals(clone))
			updateMemberExtra(clone);
		return clone.getId();
	}

	/**
	 * 更新会员基本信息
	 * 
	 * @param member
	 */
	private void updateMemberBase(MemberEntity member) {
		String sql = getExecSql("update_member_base");
		Integer result = getNamedParameterJdbcTemplate().update(sql, member.toStorageMap());
		Preconditions.checkState(1 == result, String.format("修改会员[%s]基本信息失败", member.getId()));
	}

	/**
	 * 更新会员附加信息
	 * 
	 * @param memberAddition
	 */
	private void updateMemberAddition(MemberAdditionEntity memberAddition) {
		String sql = getExecSql("update_member_addition");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberAddition.toStorageMap());
		Preconditions.checkState(1 == result, String.format("修改会员[%s]附加信息失败", memberAddition.getId()));
	}

	/**
	 * 更新会员卡信息
	 * 
	 * @param memberCard
	 */
	private void updateMemberCard(MemberCardEntity memberCard) {
		String sql = getExecSql("update_member_card");
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberCard.toStorageMap());
		Preconditions.checkState(1 == result, String.format("修改会员[%s]卡信息失败", memberCard.getId()));
	}

	/**
	 * 修改会员扩展信息
	 * 
	 * @param memberExtra
	 */
	private void updateMemberExtra(MemberExtraEntity memberExtra) {
		String sql = getExecSql("updateMemberExtra", null);
		Integer result = getNamedParameterJdbcTemplate().update(sql, memberExtra.toStorageMap());
		Preconditions.checkState(1 == result, String.format("修改会员[%s]扩展信息失败", memberExtra.getId()));
	}

	/**
	 * 批量启用会员
	 * 
	 * @param store
	 * @param memberIds
	 */
	public void enableMembers(StoreEntity store, List<Integer> memberIds) {
		Objects.requireNonNull(store, "门店不能为空");
		if (CollectionUtils.isEmpty(memberIds))
			return;
		List<MemberEntity> members = findMembersByIds(store, memberIds);
		enableMembers(store, members);
	}

	/**
	 * 批量启用会员
	 * 
	 * @param store
	 * @param members
	 */
	public void enableMembers(StoreEntity store, Collection<MemberEntity> members) {
		if (CollectionUtils.isEmpty(members))
			return;
		List<Integer> disableMemberIds = members.stream().filter(x -> !x.isEnable()).map(y -> y.getId())
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(disableMemberIds))
			return;
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberIds", disableMemberIds);
		params.put("effectFlag", 1);
		String sql = getExecSql("update_effective", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	/**
	 * 批量禁止会员
	 * 
	 * @param store
	 * @param memberIds
	 */
	public void disableMembers(StoreEntity store, List<Integer> memberIds) {
		Objects.requireNonNull(store, "门店不能为空");
		if (CollectionUtils.isEmpty(memberIds))
			return;
		List<MemberEntity> members = findMembersByIds(store, memberIds);
		disableMembers(store, members);
	}

	/**
	 * 批量禁止会员
	 * 
	 * @param store
	 * @param members
	 */
	public void disableMembers(StoreEntity store, Collection<MemberEntity> members) {
		if (CollectionUtils.isEmpty(members))
			return;
		List<Integer> enableMemberIds = members.stream().filter(x -> x.isEnable()).map(y -> y.getId())
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(enableMemberIds))
			return;
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberIds", enableMemberIds);
		params.put("effectFlag", 0);
		String sql = getExecSql("update_effective", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	/**
	 * 批量将会员分配给导购
	 * 
	 * @param store
	 * @param memberIds
	 * @param employee
	 */
	public void allot(StoreEntity store, Collection<Integer> memberIds, EmployeeEntity employee) {
		Objects.requireNonNull(store, "入参门店不能为空");
		Objects.requireNonNull(employee, "入参employee不能为空");
		if (CollectionUtils.isEmpty(memberIds))
			return;
		List<MemberEntity> members = findMembersByIds(store, memberIds).stream().filter(x -> !x.isAllot(employee))
				.collect(Collectors.toList());
		if(CollectionUtils.isEmpty(members)) return ;
		members.forEach(x -> x.allot(employee));
		List<Integer> allotMemberIds = members.stream().map(x -> x.getId()).collect(Collectors.toList());
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberIds", allotMemberIds);
		params.put("employeeId", employee.getId());
		String sql = getExecSql("allot_members_to_employee", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	/**
	 * 批量解除分配
	 * 
	 * @param store
	 * @param memberIds
	 * @param employee
	 */
	public void deallocate(StoreEntity store, Collection<Integer> memberIds) {
		Objects.requireNonNull(store, "入参门店不能为空");
		if (CollectionUtils.isEmpty(memberIds))
			return;
		List<MemberEntity> members = findMembersByIds(store, memberIds);
		List<Integer> deallocateMemberIds = members.stream().map(x -> x.getId()).collect(Collectors.toList());
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberIds", deallocateMemberIds);
		String sql = getExecSql("deallocate_members", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	/**
	 * 转移会员
	 * 
	 * @param scource
	 * @param dest
	 */
	public void transferMembers(EmployeeEntity scource, EmployeeEntity dest) {
		Objects.requireNonNull(scource, "入参scource不能为空");
		Objects.requireNonNull(dest, "入参dest不能为空");
		Map<String, Object> params = Maps.newHashMap();
		params.put("scourceEmpId", scource.getId());
		params.put("destEmpId", dest.getId());
		String sql = getExecSql("transfer_members");
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	/**
	 * 标记会员电话号码为有效
	 * 
	 * @param store
	 * @param memberIds
	 */
	public void activateMobilePhone(StoreEntity store, Collection<Integer> memberIds) {
		Objects.requireNonNull(store, "入参门店store不能为空");
		if (CollectionUtils.isEmpty(memberIds))
			return;
		List<MemberEntity> members = findMembersByIds(store, memberIds).stream().filter(x -> !x.isReachable())
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(members))
			return;
		Set<Integer> unReacheableMemberIds = members.stream().map(x -> x.getId()).collect(Collectors.toSet());
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberIds", unReacheableMemberIds);
		String sql = getExecSql("update_reachable", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	/**
	 * 将会员的生日转换为农历生日
	 * 
	 * @param store
	 * @param memberIds
	 */
	public void toLunarBirthDay(StoreEntity store, Collection<Integer> memberIds) {
		if (CollectionUtils.isEmpty(memberIds))
			return;
		List<MemberEntity> members = findMembersByIds(store, memberIds).stream().filter(x -> x.hasGregorianBirthday())
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(members))
			return;
		Set<Integer> updateMemberIds = members.stream().map(x -> x.getId()).collect(Collectors.toSet());
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberIds", updateMemberIds);
		String sql = getExecSql("to_lunar", params);
		getNamedParameterJdbcTemplate().update(sql, Maps.newHashMap());
	}

	/**
	 * 一键分配未分配会员给导购
	 * 
	 * @param store
	 */
	public void oneKeyAllotMembers(StoreEntity store, List<EmployeeEntity> employees) {
		Objects.requireNonNull(store, "门店不能为空");
		if (CollectionUtils.isEmpty(employees))
			return;
		Date now = new Date();
		List<MemberEntity> members = loadUnallocatedMembers(store);
		if (CollectionUtils.isEmpty(members))
			return;
		Map<String, Object>[] memberMaps = new Map[members.size()];
		for (int i = 0; i < members.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("employeeId", employees.get(i % employees.size()).getId());
			map.put("memberId", members.get(i).getId());
			map.put("storeId", store.getId());
			map.put("time", now);
			memberMaps[i] = map;
		}
		String memberSql = getExecSql("update_employee_member", null);
		getNamedParameterJdbcTemplate().batchUpdate(memberSql, memberMaps);
		String recordSql = getExecSql("insert_assgin_record", null);
		getNamedParameterJdbcTemplate().batchUpdate(recordSql, memberMaps);
	}

	/**
	 * 指定分配未分配的会员
	 * 
	 * @param store
	 * @param assigns
	 */
	public void assignRandomMembers(StoreEntity store, List<AssignDTO> assigns) {
		Objects.requireNonNull(store);
		if (CollectionUtils.isEmpty(assigns))
			return;
		Map<String, Object>[] memberMaps = new Map[assigns.size()];
		for (int i = 0; i < assigns.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("employeeId", assigns.get(i).getEmployeeId());
			map.put("count", assigns.get(i).getCount());
			map.put("storeId", store.getId());
			memberMaps[i] = map;
		}
		String sql = getExecSql("assign_random_members");
		getNamedParameterJdbcTemplate().batchUpdate(sql, memberMaps);
	}

	/**
	 * 还原已一键分配的会员，设置为未分配状态
	 * 
	 * @param store
	 */
	public void deallocateMembers(StoreEntity store) {
		Objects.requireNonNull(store, "门店不能为空");
		Map<String, Object> params = Maps.newHashMap();
		params.put("storeId", store.getId());
		String clearSql = getExecSql("clear_guide_4onekey", params);
		getNamedParameterJdbcTemplate().update(clearSql, params);
	}

	/**
	 * 解除导购分配的会员
	 * 
	 * @param employee
	 */
	public void deallocateMembers(StoreEntity store, EmployeeEntity employee) {
		Objects.requireNonNull(employee, "导购不能为空");
		Map<String, Object> params = Maps.newHashMap();
		params.put("storeId", store.getId());
		params.put("employeeId", employee.getId());
		String sql = getExecSql("clear_guide_4Emp", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	/**
	 * 重新分配会员
	 * 
	 * @param sourceEmp
	 *            原导购
	 * @param destEmp
	 *            目标导购
	 * @param members
	 *            待分配会员
	 */
	public void reassignMembers(EmployeeEntity sourceEmp, EmployeeEntity destEmp, List<MemberEntity> members) {
		Objects.requireNonNull(sourceEmp, "原导购不能为空");
		Objects.requireNonNull(destEmp, "目标导购不能为空");
		if (CollectionUtils.isEmpty(members))
			return;
		Map<String, Object> params = Maps.newHashMap();
		params.put("sourceEmpId", sourceEmp.getId());
		params.put("destEmpId", destEmp.getId());
		List<Integer> memberIds = Lists.newArrayList();
		for (MemberEntity member : members)
			memberIds.add(member.getId());
		params.put("memberIds", memberIds);
		String sql = getExecSql("reassign_members", params);
		getNamedParameterJdbcTemplate().update(sql, params);
	}

	// 返回DTO 用于计算与微信好友的匹配度
	public Optional<List<Member4MatchWebChatDto>> find4MatchWebChatByStore(StoreEntity store) {
		Preconditions.checkNotNull(store);
		Map<String, Object> params = Maps.newHashMap();
		params.put("storeId", store.getId());
		List<Member4MatchWebChatDto> list = getNamedParameterJdbcTemplate()
				.query(getExecSql("find_matchwebchat_bystore", params), params, new RowMapperMember4MatchWebChatDto());
		if (logger.isDebugEnabled())
			logger.debug(String.format("find4MatchWebChatByStore(%s) return -> dto number is %s", store.getName(),
					org.springframework.util.CollectionUtils.isEmpty(list) ? 0 : list.size()));
		if(CollectionUtils.isEmpty(list)) return Optional.empty();
		return Optional.of(list);
	}
	
	class RowMapperMember4MatchWebChatDto implements RowMapper<Member4MatchWebChatDto> {
		@Override
		public Member4MatchWebChatDto mapRow(ResultSet rs, int i) throws SQLException {
			return new Member4MatchWebChatDto(rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
					rs.getInt("sex"), rs.getString("srfm"), rs.getString("crfm"), rs.getDate("lastVisitTime"),					
					rs.getString("memberCardNum"));
		}
	}
	
	@Override
	protected ResultSetExtractor<MemberEntity> getResultSetExtractor() {
		// TODO Auto-generated method stub
		return new ResultSetExtractorImpl();
	}

	class RowMapperImpl implements RowMapper<MemberEntity> {
		@Override
		public MemberEntity mapRow(ResultSet resultSet, int i) throws SQLException {
			return buildByResultSet(resultSet);
		}
	}

	class ResultSetExtractorImpl implements ResultSetExtractor<MemberEntity> {

		@Override
		public MemberEntity extractData(ResultSet resultSet) throws SQLException, DataAccessException {
			if (resultSet.next()) {
				return buildByResultSet(resultSet);
			}
			return null;
		}
	}

	private MemberEntity buildByResultSet(ResultSet resultSet) throws SQLException {
		List<Integer> remarkIds = Lists.newArrayList();
		List<Integer> labelIds = Lists.newArrayList();
		List<Integer> storeIds = Lists.newArrayList();
		String remarkIdsStr = resultSet.getString("remarkIds");
		if (!Strings.isNullOrEmpty(remarkIdsStr))
			remarkIds = Splitter.on(",").splitToList(remarkIdsStr).stream().map(Integer::valueOf)
					.collect(Collectors.toList());
		String labelIdsStr = resultSet.getString("labelIds");
		if (!Strings.isNullOrEmpty(labelIdsStr))
			labelIds = Splitter.on(",").splitToList(labelIdsStr).stream().map(Integer::valueOf)
					.collect(Collectors.toList());
		String storeIdsStr = resultSet.getString("storeIds");
		if (!Strings.isNullOrEmpty(storeIdsStr))
			storeIds = Splitter.on(",").splitToList(storeIdsStr).stream().map(Integer::valueOf)
					.collect(Collectors.toList());

		return MemberEntity.valueOf(resultSet.getInt("id"), resultSet.getString("iconUrl"), resultSet.getString("name"),
				resultSet.getString("namePinyin"), resultSet.getInt("sex"), resultSet.getObject("birthday") == null ? null  : resultSet.getDate("birthday"),
						resultSet.getObject("lunarBirthday") == null ? null : resultSet.getDate("lunarBirthday"), resultSet.getInt("calendarType"),
				resultSet.getString("detailAddress"), resultSet.getInt("memberType"),
				resultSet.getString("certificate"), resultSet.getInt("certificateType"),
				resultSet.getInt("serviceLevel"), remarkIds, labelIds, resultSet.getString("email"),
				resultSet.getString("mobilephone"), resultSet.getString("telephone"), resultSet.getInt("reachable"),
				resultSet.getString("qqNum"), resultSet.getString("weiboNum"), resultSet.getString("weixinId"),
				resultSet.getInt("guideId"), resultSet.getString("oldMemberCode"),
				resultSet.getInt("oldShoppingGuideId"), resultSet.getInt("oldStoreId"),
				resultSet.getInt("effectiveFlag"), resultSet.getInt("status"), resultSet.getInt("companyId"),
				resultSet.getInt("companyId1"), resultSet.getInt("companyId2"), storeIds,
				resultSet.getString("sourceFrom"), resultSet.getInt("sourceChannel"),
				resultSet.getInt("memberCardType"), resultSet.getString("memberCardNum"),
				resultSet.getInt("createStoreId"), resultSet.getObject("createCardTime") == null ? null : resultSet.getDate("createCardTime"), resultSet.getInt("limitday"),
				resultSet.getInt("totalScore"), resultSet.getString("carePeople"), resultSet.getInt("characterType"),
				resultSet.getInt("faithType"), resultSet.getString("hobby"), resultSet.getString("idols"),
				resultSet.getString("jobType"), resultSet.getString("likeBrand"), resultSet.getInt("likeContact"),
				resultSet.getInt("marryStatus"), resultSet.getString("specialDay"), resultSet.getInt("zodiac"),
				resultSet.getInt("education"), resultSet.getInt("likeContactTime"),
				resultSet.getBigDecimal("rechargeAmount"), resultSet.getString("rfm"),
				resultSet.getBigDecimal("firstSaleRecordAmount"), resultSet.getString("firstSaleRecordNo"),
				resultSet.getInt("consumeTotalCount"), resultSet.getInt("consumeTotalCountCurYear"),
				resultSet.getBigDecimal("maxConsumePrice"), resultSet.getBigDecimal("maxConsumePriceCurYear"),
				resultSet.getBigDecimal("totalConsumeAmount"), resultSet.getBigDecimal("totalConsumeAmountCurYear"),
				resultSet.getObject("lastVisitTime") == null ? null : resultSet.getDate("lastVisitTime"));
	}

	private ResultSetExtractor<MemberExtraEntity> getMemberExtraResultSetExtractor() {
		return new ResultSetExtractor<MemberExtraEntity>() {
			@Override
			public MemberExtraEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
				MemberExtraEntity result = null;
				while (rs.next()) {
					Integer memberId = ResultSetUtil.getOptValue(rs, "memberId", Integer.class).orNull();
					if (null == memberId)
						return result;
					String jacketSize = ResultSetUtil.getOptValue(rs, "jacketSize", String.class).orNull();
					String bottomsSize = ResultSetUtil.getOptValue(rs, "bottomsSize", String.class).orNull();
					String braSize = ResultSetUtil.getOptValue(rs, "braSize", String.class).orNull();
					String briefsSize = ResultSetUtil.getOptValue(rs, "briefsSize", String.class).orNull();
					String shoeSize = ResultSetUtil.getOptValue(rs, "shoeSize", String.class).orNull();
					BigDecimal chest = ResultSetUtil.getOptValue(rs, "chest", BigDecimal.class).orNull();
					BigDecimal clothingLong = ResultSetUtil.getOptValue(rs, "clothingLong", BigDecimal.class).orNull();
					BigDecimal sleeveLength = ResultSetUtil.getOptValue(rs, "sleeveLength", BigDecimal.class).orNull();
					BigDecimal shoulder = ResultSetUtil.getOptValue(rs, "shoulder", BigDecimal.class).orNull();
					BigDecimal waistline = ResultSetUtil.getOptValue(rs, "waistline", BigDecimal.class).orNull();
					BigDecimal hipline = ResultSetUtil.getOptValue(rs, "hipline", BigDecimal.class).orNull();
					BigDecimal thighCircumference = ResultSetUtil
							.getOptValue(rs, "thighCircumference", BigDecimal.class).orNull();
					BigDecimal kneeCircumference = ResultSetUtil.getOptValue(rs, "kneeCircumference", BigDecimal.class)
							.orNull();
					BigDecimal trouserLeg = ResultSetUtil.getOptValue(rs, "trouserLeg", BigDecimal.class).orNull();
					BigDecimal beforeFork = ResultSetUtil.getOptValue(rs, "beforeFork", BigDecimal.class).orNull();
					BigDecimal afterFork = ResultSetUtil.getOptValue(rs, "afterFork", BigDecimal.class).orNull();
					BigDecimal outseam = ResultSetUtil.getOptValue(rs, "outseam", BigDecimal.class).orNull();
					BigDecimal onChest = ResultSetUtil.getOptValue(rs, "onChest", BigDecimal.class).orNull();
					BigDecimal underChest = ResultSetUtil.getOptValue(rs, "underChest", BigDecimal.class).orNull();
					BigDecimal footLength = ResultSetUtil.getOptValue(rs, "footLength", BigDecimal.class).orNull();
					Integer status = ResultSetUtil.getOptValue(rs, "status", Integer.class).orNull();
					result = new MemberExtraEntity(memberId, jacketSize, bottomsSize, braSize, briefsSize, shoeSize,
							chest, clothingLong, sleeveLength, shoulder, waistline, hipline, thighCircumference,
							kneeCircumference, trouserLeg, beforeFork, afterFork, outseam, onChest, underChest,
							footLength, status);
				}
				return result;
			}
		};
	}
}
