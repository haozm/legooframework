package com.csosm.module.member.entity;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.Replaceable;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MemberEntity extends BaseEntity<Integer> implements Replaceable {
	// 会员头像
	private String iconUrl;
	// 会员名称
	private String name;
	// 会员名称短称
	private String namePinyin;
	// 会员性别 1:男 2:女
	private Integer sex = 1;
	// 会员公历生日
	private Date birthday;
	// 会员农历生日
	private Date lunarBirthday;
	// 生日类型
	private Integer calendarType;
	// 会员类型
	private Integer memberType;
	// 会员服务等级
	private Integer serviceLevel;
	// 会员备注
	private List<Integer> remarkIds;
	// 会员标签
	private List<Integer> labelIds;
	// 会员邮件
	private String email;
	// 会员手机号
	private String mobilephone;
	// 标记手机是否有效 0 代表不可达 1代表可达
	private Integer reachable = 1;
	// 会员固定电话
	private String telephone;
	// 会员QQ
	private String qqNum;
	// 会员微博
	private String weiboNum;
	// 会员微信ID
	private String weixinId;
	// 会员服务导购ID
	private Integer guideId;
	// 旧会员号
	private String oldMemberCode;
	// 旧会员导购ID
	private Integer oldShoppingGuideId;
	// 旧会员门店
	private Integer oldStoreId;
	// 是否启用,// 有效标准 0无效 1有效
	private Integer effectFlag = 1;
	// 是否已删除 0为删除 1为未删除
	private Integer status = 1;
	// 默认公司ID
	private Integer companyId;
	// 公司ID1
	private Integer companyId1;
	// 公司ID2
	private Integer companyId2;
	// 门店集合
	private List<Integer> storeIds;
	// 会员数据来源
	private String sourceFrom;
	// 会员数据来源渠道
	private Integer sourceChannel;
	// 会员附加信息
	private MemberAdditionEntity memberAddition;
	// 会员卡信息
	private MemberCardEntity memberCard;
	// 会员消费信息
	private MemberConsumeEntity memberConsume;

	private MemberEntity(Integer id, String iconUrl, String name, String namePinyin, Integer sex, Date birthday,
			Date lunarBirthday, Integer calendarType, Integer memberType, Integer serviceLevel, List<Integer> remarkIds,
			List<Integer> labelIds, String email, String mobilephone, String telephone, Integer reachable, String qqNum,
			String weiboNum, String weixinId, Integer guideId, String oldMemberCode, Integer oldShoppingGuideId,
			Integer oldStoreId, Integer effectFlag, Integer status, Integer companyId, Integer companyId1,
			Integer companyId2, List<Integer> storeIds, String sourceFrom, Integer sourceChannel,
			MemberAdditionEntity memberAddition, MemberCardEntity memberCard, MemberConsumeEntity memberConsume) {
		super(id);
		this.iconUrl = iconUrl;
		this.name = name;
		this.namePinyin = namePinyin;
		this.sex = sex;
		this.birthday = birthday;
		this.lunarBirthday = lunarBirthday;
		this.calendarType = calendarType;
		this.memberType = memberType;
		this.serviceLevel = serviceLevel;
		this.remarkIds = remarkIds;
		this.labelIds = labelIds;
		this.email = email;
		this.mobilephone = mobilephone;
		this.telephone = telephone;
		this.qqNum = qqNum;
		this.weiboNum = weiboNum;
		this.weixinId = weixinId;
		this.guideId = guideId;
		this.oldMemberCode = oldMemberCode;
		this.oldShoppingGuideId = oldShoppingGuideId;
		this.oldStoreId = oldStoreId;
		this.effectFlag = effectFlag;
		this.status = status;
		this.companyId = companyId;
		this.companyId1 = companyId1;
		this.companyId2 = companyId2;
		this.storeIds = storeIds;
		this.sourceFrom = sourceFrom;
		this.sourceChannel = sourceChannel;
		this.reachable = reachable;
		this.memberAddition = memberAddition;
		this.memberCard = memberCard;
		this.memberConsume = memberConsume;
	}

	private MemberEntity(Integer id, String name, Integer sex, String mobilePhone, Integer serviceLevel,
			EmployeeEntity employee, StoreEntity store) {
		super(id);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "会员名称不能为空");
		Objects.requireNonNull(sex, "会员性别不能为空");
		Objects.requireNonNull(store, "会员门店不能为空");
		Preconditions.checkState(store.getCompanyId().isPresent(), String.format("会员门店[%s]无公司信息", store.getId()));
		this.name = name;
		this.sex = sex;
		this.mobilephone = mobilePhone;
		this.serviceLevel = serviceLevel;
		this.guideId = null == employee ? null : employee.getId();
		this.storeIds = Lists.newArrayList(store.getId());
		this.companyId = store.getCompanyId().get();
	}

	/**
	 * 创建具备基本信息的会员
	 * 
	 * @param name
	 *            名称
	 * @param sex
	 *            性别
	 * @param mobilePhone
	 *            电话
	 * @param serviceLevel
	 *            服务等级
	 * @param employee
	 *            服务导购
	 * @return
	 */
	public static MemberEntity createSimpleMember(Integer id, String name, Integer sex, String mobilePhone,
			Integer serviceLevel, EmployeeEntity employee, StoreEntity store) {
		return new MemberEntity(id, name, sex, mobilePhone, serviceLevel, employee, store);
	}

	// 创建信息比较多会员信息
	public static MemberEntity createMember(Integer id, String name, Integer memberType, String namePinyin, Integer sex,
			Integer serviceLevel, String phone, String telePhone, String certificate, Integer certificateType,
			Integer memberCardType, String memberCardNum, Date createCardTime, Integer createCardStoreId,
			String createCardStoreName, String weixinId, String qqNum, String weiboNum, String email,
			Date gregorianBirthday, Date lunarBirthday, Integer calendarType, String iconUrl, Integer marryStatus,
			String detailAddress, String idols, String carePeople, Integer zodiac, Integer characterType,
			String jobType, Integer faithType, String hobby, String likeBrand, Integer likeContact, String specialDay,
			Integer education, Integer likeContactTime, StoreEntity store) {
		MemberAdditionEntity memberAddition = new MemberAdditionEntity(id, carePeople, characterType, faithType, hobby,
				idols, jobType, likeBrand, likeContact, marryStatus, specialDay, zodiac, education, likeContactTime,
				certificate, certificateType, detailAddress);
		MemberCardEntity memberCard = new MemberCardEntity(id, memberCardType, memberCardNum, createCardStoreId,
				createCardTime, null, null);
		MemberEntity member = new MemberEntity(id, iconUrl, name, namePinyin, sex, gregorianBirthday, lunarBirthday,
				calendarType, memberType, serviceLevel, null, null, email, phone, telePhone, 1, qqNum, weiboNum,
				weixinId, null, null, null, null, 1, 1, store.getCompanyId().get(), null, null,
				Lists.newArrayList(store.getId()), null, null, memberAddition, memberCard, null);
		return member;
	}

	/**
	 * 从数据库中还原会员对象
	 * 
	 * @param id
	 * @param id2
	 * @param iconUrl
	 * @param name
	 * @param namePinyin
	 * @param sex
	 * @param birthday
	 * @param lunarBirthday
	 * @param calendarType
	 * @param detailAddress
	 * @param memberType
	 * @param certificate
	 * @param certificateType
	 * @param serviceLevel
	 * @param remarkIds
	 * @param labelIds
	 * @param email
	 * @param mobilephone
	 * @param telephone
	 * @param qqNum
	 * @param weiboNum
	 * @param weixinId
	 * @param guideId
	 * @param oldMemberCode
	 * @param oldShoppingGuideId
	 * @param oldStoreId
	 * @param effectFlag
	 * @param status
	 * @param companyIds
	 * @param storeIds
	 * @param sourceFrom
	 * @param sourceChannel
	 * @param memberCardType
	 * @param memberCardNum
	 * @param createStoreId
	 * @param createCardTime
	 * @param limitday
	 * @param carePeople
	 * @param characterType
	 * @param faithType
	 * @param hobby
	 * @param idols
	 * @param jobType
	 * @param likeBrand
	 * @param likeContact
	 * @param marryStatus
	 * @param specialDay
	 * @param zodiac
	 * @param education
	 * @param likeContactTime
	 * @param rechargeAmount
	 * @param rfm
	 * @param firstSaleRecordAmount
	 * @param firstSaleRecordNo
	 * @param consumeTotalCount
	 * @param consumeTotalCountCurYear
	 * @param maxConsumePrice
	 * @param maxConsumePriceCurYear
	 * @param totalConsumeAmount
	 * @param totalConsumeAmountCurYear
	 * @param lastVisitTime
	 * @return
	 */
	public static MemberEntity valueOf(Integer id, String iconUrl, String name, String namePinyin, Integer sex,
			Date birthday, Date lunarBirthday, Integer calendarType, String detailAddress, Integer memberType,
			String certificate, Integer certificateType, Integer serviceLevel, List<Integer> remarkIds,
			List<Integer> labelIds, String email, String mobilephone, String telephone, Integer reachable, String qqNum,
			String weiboNum, String weixinId, Integer guideId, String oldMemberCode, Integer oldShoppingGuideId,
			Integer oldStoreId, Integer effectFlag, Integer status, Integer companyId, Integer companyId1,
			Integer companyId2, List<Integer> storeIds, String sourceFrom, Integer sourceChannel,
			Integer memberCardType, String memberCardNum, Integer createStoreId, Date createCardTime, Integer limitday,
			Integer totalScore, String carePeople, Integer characterType, Integer faithType, String hobby, String idols,
			String jobType, String likeBrand, Integer likeContact, Integer marryStatus, String specialDay,
			Integer zodiac, Integer education, Integer likeContactTime, BigDecimal rechargeAmount, String rfm,
			BigDecimal firstSaleRecordAmount, String firstSaleRecordNo, Integer consumeTotalCount,
			Integer consumeTotalCountCurYear, BigDecimal maxConsumePrice, BigDecimal maxConsumePriceCurYear,
			BigDecimal totalConsumeAmount, BigDecimal totalConsumeAmountCurYear, Date lastVisitTime) {
		MemberAdditionEntity memberAddition = MemberAdditionEntity.valueOf(id, carePeople, characterType, faithType,
				hobby, idols, jobType, likeBrand, likeContact, marryStatus, specialDay, zodiac, education,
				likeContactTime, certificate, certificateType, detailAddress);
		MemberCardEntity memberCard = MemberCardEntity.valueOf(id, memberCardType, memberCardNum, createStoreId,
				createCardTime, limitday, totalScore);
		MemberConsumeEntity memberConsume = MemberConsumeEntity.valueOf(id, rechargeAmount, rfm, firstSaleRecordAmount,
				firstSaleRecordNo, consumeTotalCount, consumeTotalCountCurYear, maxConsumePrice, maxConsumePriceCurYear,
				totalConsumeAmount, totalConsumeAmountCurYear, lastVisitTime);
		return new MemberEntity(id, iconUrl, name, namePinyin, sex, birthday, lunarBirthday, calendarType, memberType,
				serviceLevel, remarkIds, labelIds, email, mobilephone, telephone, reachable, qqNum, weiboNum, weixinId,
				guideId, oldMemberCode, oldShoppingGuideId, oldStoreId, effectFlag, marryStatus, companyId, companyId1,
				companyId2, storeIds, sourceFrom, sourceChannel, memberAddition, memberCard, memberConsume);
	}

	/**
	 * 创建会员卡
	 * 
	 * @return
	 */
	public MemberCardEntity createSimpleMemberCard() {
		return new MemberCardEntity(this);
	}

	/**
	 * 创建会员消费信息
	 * 
	 * @return
	 */
	public MemberConsumeEntity createSimpleMemberConsume() {
		return new MemberConsumeEntity(this);
	}
	/**
	 * 创建会员附加信息
	 * @return
	 */
	public MemberAdditionEntity createSimpleMemberAddition() {
		return new MemberAdditionEntity(this);
	}
	
	/**
	 * 创建简单的会员扩展信息
	 * @return
	 */
	public MemberExtraEntity createSimpleMemberExtra() {
		return new MemberExtraEntity(this);
	}
	/**
	 * 是否已分配给了该导购
	 * 
	 * @param employee
	 * @return
	 */
	public boolean isAllot(EmployeeEntity employee) {
		return this.guideId == employee.getId();
	}
	/**
	 * 分配导购
	 * @param employee
	 */
	public void allot(EmployeeEntity employee) {
		if (this.guideId == employee.getId())
			return;
		this.guideId = employee.getId();
	}
	/**
	 * 解除分配导购
	 * @param employee
	 */
	public void deallocate(EmployeeEntity employee) {
		if (this.guideId == null)
			return;
		this.guideId = null;
	}
	
	public boolean hasMemberCard() {
		if(null == this.memberCard || null == this.memberCard.getMemberCardNum())
			return false;
		return true;
	}
	
	public boolean hasWeixin() {
		return null != this.weixinId;
	}
	/**
	 * 获取会员卡
	 * @return
	 */
	public MemberCardEntity getMemberCard(){
		return this.memberCard;
	}
	
	/**
	 * 获取会员卡号
	 * @return
	 */
	public Optional<String> getMemberCardNum(){
		return hasMemberCard()?Optional.of(this.memberCard.getMemberCardNum()):Optional.empty();
	}
	/**
	 * 获取分配导购ID
	 * 
	 * @return
	 */
	public Optional<Integer> getGuideId() {
		if (this.guideId == null)
			return Optional.empty();
		return Optional.of(this.guideId);
	}

	/**
	 * 启用会员
	 */
	public void enable() {
		if (this.effectFlag == 1)
			return;
		this.effectFlag = 1;
	}

	/**
	 * 是否已启用
	 * 
	 * @return
	 */
	public boolean isEnable() {
		return this.effectFlag == 1;
	}

	/**
	 * 禁用会员
	 */
	public void disable() {
		if (this.effectFlag == 0)
			return;
		this.effectFlag = 0;
	}

	/**
	 * 删除会员
	 */
	public void remove() {
		if (0 == this.status)
			return;
		this.status = 0;
	}

	/**
	 * 电话是否有效
	 * 
	 * @return
	 */
	public boolean isReachable() {
		return 1 == this.reachable;
	}

	/**
	 * 设置会员电话有效
	 */
	public void reachable() {
		if (1 == this.reachable)
			return;
		this.reachable = reachable;
	}

	/**
	 * 会员生日是否农历生日
	 * 
	 * @return
	 */
	public boolean hasLunarBirthday() {
		return this.calendarType == 2 && null != this.lunarBirthday;
	}

	/**
	 * 当前会员是否有公历生日
	 * 
	 * @return
	 */
	public boolean hasGregorianBirthday() {
		return null != this.birthday && this.calendarType == 1;
	}

	public MemberEntity modify(String iconUrl, String name, String namePinyin, Integer sex, Date birthday,
			Date lunarBirthday, Integer calendarType, Integer memberType, Integer serviceLevel, String email,
			String mobilephone, String telephone, String qqNum, String weiboNum, String weixinId) {
		MemberEntity clone = null;
		try {
			clone = (MemberEntity) this.clone();
			clone.name = name;
			clone.sex = sex;
			clone.birthday = birthday;
			clone.serviceLevel = serviceLevel;
			clone.memberType = memberType;
			clone.mobilephone = mobilephone;
			clone.qqNum = qqNum;
			clone.weixinId = weixinId;
			clone.weiboNum = weiboNum;
			clone.email = email;
			clone.lunarBirthday = lunarBirthday;
			clone.iconUrl = iconUrl;
			clone.calendarType = calendarType;
			clone.namePinyin = namePinyin;
			clone.telephone = telephone;
			clone.namePinyin = namePinyin;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("克隆会员基本信息发生异常", e);
		}
		return clone;
	}

	// Optional<String> Optional<Integer> Optional<Date>

	public MemberEntity modify(Optional<String> iconUrlOpt, String name, Optional<String> namePinyinOpt, Integer sex,
			Optional<Date> birthdayOpt, Optional<Date> lunarBirthdayOpt, Optional<Integer> calendarTypeOpt,
			Optional<Integer> memberTypeOpt, Optional<Integer> serviceLevelOpt, Optional<String> emailOpt,
			Optional<String> mobilephoneOpt, Optional<String> telephoneOpt, Optional<String> qqNumOpt,
			Optional<String> weiboNumOpt, Optional<String> weixinIdOpt) {
		MemberEntity clone = null;
		try {
			clone = (MemberEntity) this.clone();
			clone.name = name;
			clone.sex = sex;
			if (birthdayOpt.isPresent())
				clone.birthday = birthdayOpt.get();
			if (serviceLevelOpt.isPresent())
				clone.serviceLevel = serviceLevelOpt.get();
			if (memberTypeOpt.isPresent())
				clone.memberType = memberTypeOpt.get();
			if (mobilephoneOpt.isPresent())
				clone.mobilephone = mobilephoneOpt.get();
			if (qqNumOpt.isPresent())
				clone.qqNum = qqNumOpt.get();
			if (weixinIdOpt.isPresent())
				clone.weixinId = weixinIdOpt.get();
			if (weiboNumOpt.isPresent())
				clone.weiboNum = weiboNumOpt.get();
			if (emailOpt.isPresent())
				clone.email = emailOpt.get();
			if (lunarBirthdayOpt.isPresent())
				clone.lunarBirthday = lunarBirthdayOpt.get();
			if (iconUrlOpt.isPresent())
				clone.iconUrl = iconUrlOpt.get();
			if (calendarTypeOpt.isPresent())
				clone.calendarType = calendarTypeOpt.get();
			if (namePinyinOpt.isPresent())
				clone.namePinyin = namePinyinOpt.get();
			if (telephoneOpt.isPresent())
				clone.telephone = telephoneOpt.get();
			if (namePinyinOpt.isPresent())
				clone.namePinyin = namePinyinOpt.get();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("克隆会员基本信息发生异常", e);
		}
		return clone;
	}

	/**
	 * 设置创建者
	 * 
	 * @param user
	 */
	public void setCreator(LoginUserContext user) {
		this.setCreateUserId(user.getUserId());
	}

	/**
	 * 设置修改人
	 * 
	 * @param user
	 */
	public void setModifyUser(LoginUserContext user) {
		this.setModifyUserId(user.getUserId());
	}

	/**
	 * 转换为与数据库对接的MAP
	 * 
	 * @return
	 */
	public Map<String, Object> toStorageMap() {
		Map<String, Object> map = super.toMap();
		map.put("name", this.name);
		map.put("sex", this.sex);
		map.put("mobilePhone", this.mobilephone);
		map.put("serviceLevel", this.serviceLevel);
		map.put("guideId", this.guideId);
		map.put("iconUrl", this.iconUrl);
		map.put("namePinyin", this.namePinyin);
		map.put("birthday", this.birthday);
		map.put("lunarBirthday", this.lunarBirthday);
		map.put("calendarType", this.calendarType);
		map.put("memberType", this.memberType);
		map.put("remarkIds", CollectionUtils.isEmpty(remarkIds) ? "" : Joiner.on(",").join(this.remarkIds));
		map.put("labelIds", CollectionUtils.isEmpty(labelIds) ? "" : Joiner.on(",").join(this.labelIds));
		map.put("email", this.email);
		map.put("reachable", this.reachable);
		map.put("telephone", this.telephone);
		map.put("qqNum", this.qqNum);
		map.put("weiboNum", this.weiboNum);
		map.put("weixinId", this.weixinId);
		map.put("oldMemberCode", this.oldMemberCode);
		map.put("oldShoppingGuideId", this.oldShoppingGuideId);
		map.put("effectFlag", this.effectFlag);
		map.put("oldStoreId", this.oldStoreId);
		map.put("status", this.status);
		map.put("companyId", this.companyId);
		map.put("companyId1", this.companyId1);
		map.put("companyId2", this.companyId2);
		map.put("sourceFrom", this.sourceFrom);
		map.put("sourceChannel", this.sourceChannel);
		map.put("storeIds", CollectionUtils.isEmpty(storeIds) ? "" : Joiner.on(",").join(this.storeIds));
		return map;
	}

	/**
	 * 转换为与数据库对接的MAP
	 * 
	 * @return
	 */
	public Map<String, Object> toViewMap() {
		return this.toStorageMap();
	}
	
	public String getIconUrl() {
		return iconUrl;
	}

	public String getName() {
		return name;
	}

	public String getNamePinyin() {
		return namePinyin;
	}

	public Integer getSex() {
		return sex;
	}

	public Date getBirthday() {
		return birthday;
	}

	public Date getLunarBirthday() {
		return lunarBirthday;
	}

	public Integer getCalendarType() {
		return calendarType;
	}

	public Integer getMemberType() {
		return memberType;
	}

	public Integer getServiceLevel() {
		return serviceLevel;
	}

	public List<Integer> getRemarkIds() {
		return remarkIds;
	}

	public List<Integer> getLabelIds() {
		return labelIds;
	}

	public String getEmail() {
		return email;
	}

	public String getMobilephone() {
		return mobilephone;
	}

	public Integer getReachable() {
		return reachable;
	}

	public String getTelephone() {
		return telephone;
	}

	public String getQqNum() {
		return qqNum;
	}

	public String getWeiboNum() {
		return weiboNum;
	}

	public String getWeixinId() {
		return weixinId;
	}

	public String getOldMemberCode() {
		return oldMemberCode;
	}

	public Integer getOldShoppingGuideId() {
		return oldShoppingGuideId;
	}

	public Integer getOldStoreId() {
		return oldStoreId;
	}

	public Integer getEffectFlag() {
		return effectFlag;
	}

	public Integer getStatus() {
		return status;
	}

	public List<Integer> getStoreIds() {
		return storeIds;
	}

	public String getSourceFrom() {
		return sourceFrom;
	}

	public Integer getSourceChannel() {
		return sourceChannel;
	}

	public MemberAdditionEntity getMemberAddition() {
		return memberAddition;
	}


	public MemberConsumeEntity getMemberConsume() {
		return memberConsume;
	}
	
	public Optional<Integer> getStoreId(){
		if(CollectionUtils.isEmpty(this.storeIds)) return Optional.empty();
		Preconditions.checkState(this.storeIds.size() == 1, "会员属于多个门店");
		return Optional.of(this.storeIds.get(0));
	}
	
	public Integer getCompanyId() {
		return companyId;
	}

	public Integer getCompanyId1() {
		return companyId1;
	}

	public Integer getCompanyId2() {
		return companyId2;
	}

	public boolean equalsBaseInfo(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		MemberEntity other = (MemberEntity) obj;
		if (!this.getId().equals(other.getId()))
			return false;
		if (birthday == null) {
			if (other.birthday != null)
				return false;
		} else if (!birthday.equals(other.birthday))
			return false;
		if (calendarType == null) {
			if (other.calendarType != null)
				return false;
		} else if (!calendarType.equals(other.calendarType))
			return false;
		if (companyId == null) {
			if (other.companyId != null)
				return false;
		} else if (!companyId.equals(other.companyId))
			return false;
		if (companyId1 == null) {
			if (other.companyId1 != null)
				return false;
		} else if (!companyId1.equals(other.companyId1))
			return false;
		if (companyId2 == null) {
			if (other.companyId2 != null)
				return false;
		} else if (!companyId2.equals(other.companyId2))
			return false;
		if (effectFlag == null) {
			if (other.effectFlag != null)
				return false;
		} else if (!effectFlag.equals(other.effectFlag))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (guideId == null) {
			if (other.guideId != null)
				return false;
		} else if (!guideId.equals(other.guideId))
			return false;
		if (iconUrl == null) {
			if (other.iconUrl != null)
				return false;
		} else if (!iconUrl.equals(other.iconUrl))
			return false;
		if (lunarBirthday == null) {
			if (other.lunarBirthday != null)
				return false;
		} else if (!lunarBirthday.equals(other.lunarBirthday))
			return false;
		if (memberType == null) {
			if (other.memberType != null)
				return false;
		} else if (!memberType.equals(other.memberType))
			return false;
		if (mobilephone == null) {
			if (other.mobilephone != null)
				return false;
		} else if (!mobilephone.equals(other.mobilephone))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (namePinyin == null) {
			if (other.namePinyin != null)
				return false;
		} else if (!namePinyin.equals(other.namePinyin))
			return false;
		if (oldMemberCode == null) {
			if (other.oldMemberCode != null)
				return false;
		} else if (!oldMemberCode.equals(other.oldMemberCode))
			return false;
		if (oldShoppingGuideId == null) {
			if (other.oldShoppingGuideId != null)
				return false;
		} else if (!oldShoppingGuideId.equals(other.oldShoppingGuideId))
			return false;
		if (oldStoreId == null) {
			if (other.oldStoreId != null)
				return false;
		} else if (!oldStoreId.equals(other.oldStoreId))
			return false;
		if (qqNum == null) {
			if (other.qqNum != null)
				return false;
		} else if (!qqNum.equals(other.qqNum))
			return false;
		if (reachable == null) {
			if (other.reachable != null)
				return false;
		} else if (!reachable.equals(other.reachable))
			return false;
		if (serviceLevel == null) {
			if (other.serviceLevel != null)
				return false;
		} else if (!serviceLevel.equals(other.serviceLevel))
			return false;
		if (sex == null) {
			if (other.sex != null)
				return false;
		} else if (!sex.equals(other.sex))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (telephone == null) {
			if (other.telephone != null)
				return false;
		} else if (!telephone.equals(other.telephone))
			return false;
		if (weiboNum == null) {
			if (other.weiboNum != null)
				return false;
		} else if (!weiboNum.equals(other.weiboNum))
			return false;
		if (weixinId == null) {
			if (other.weixinId != null)
				return false;
		} else if (!weixinId.equals(other.weixinId))
			return false;
		return true;
	}
	
	@Override
	public Map<String, String> toSmsMap(StoreEntity store) {
        Map<String, String> sms = Maps.newHashMap();
        sms.put("{会员姓名}", getName() == null ? "" : getName());
        sms.put("{会员编号}", this.getId() == null ? "" : this.getId().toString());

        String sex = "";
        if (this.sex != null) {
            if (this.sex == 1) {
                sex = "男";
            } else if (this.sex == 2) {
                sex = "女";
            } else {
                sex = "其他";
            }
        } else {
            sex = "其他";
        }

        sms.put("{会员性别}", sex);

        String birthday = "";
        if (this.getCalendarType() == 1) {
            birthday = this.getBirthday() != null
                    ? String.format("公历[%s]", DateFormatUtils.format(this.getBirthday(), "yyyy-MM-dd"))
                    : "";
        }
        if (this.getCalendarType() == 2) {
            birthday = this.getLunarBirthday() != null
                    ? String.format("农历[%s]", DateFormatUtils.format(this.getBirthday(), "yyyy-MM-dd"))
                    : "";
        }
        sms.put("{会员生日}", birthday);

        String serviceLevel = "";
        if (this.serviceLevel != null) {
            if (this.serviceLevel == 1) {
                serviceLevel = "粉丝服务";
            } else if (this.serviceLevel == 2) {
                serviceLevel = "积分服务";
            } else if (this.serviceLevel == 3) {
                serviceLevel = "储值服务";
            }
        } else {
            serviceLevel = "未知等级";
        }

        sms.put("{会员服务等级}", serviceLevel);

        sms.put("{会员电话号码}", this.getMobilephone() == null ? "" : this.getMobilephone());

        String memberType = "";
        if (this.memberType != null) {
            if (this.memberType == 1) memberType = "粉丝会员";
            if (this.memberType == 2) memberType = "普通会员";
        } else {
            memberType = "未知类型";
        }

        sms.put("{会员类型}", memberType);

        sms.put("{会员卡号}", !this.hasMemberCard() ? "" : null == this.memberCard.getMemberCardNum()?"":this.memberCard.getMemberCardNum());
        sms.put("{开卡时间}", !this.hasMemberCard() ? "" : this.memberCard.getCreateCardTime() != null ? DateFormatUtils.format(this.memberCard.getCreateCardTime(), "yyyy-MM-dd")
                : "");
        sms.put("{会员QQ号}", this.qqNum == null ? "" : this.qqNum);
        sms.put("{会员微信账号}", this.weixinId == null ? "" : this.weixinId);
        sms.put("{会员微博账号}", this.weiboNum == null ? "" : this.weiboNum);
        sms.put("{会员邮箱}", this.email == null ? "" : this.email);

        String marryStatus = "";
        if (null != this.memberAddition && this.memberAddition.getMarryStatus() != null) {
            if (this.memberAddition.getMarryStatus() == 0) marryStatus = "未婚";
            if (this.memberAddition.getMarryStatus() == 1) marryStatus = "已婚";
            if (this.memberAddition.getMarryStatus() == 2) marryStatus = "离异";
            if (this.memberAddition.getMarryStatus() == 3) marryStatus = "再婚";
        } else {
            marryStatus = "未知";
        }

        sms.put("{会员婚姻状况}", marryStatus);
        sms.put("{会员详细地址}", null == this.memberAddition ? "":this.memberAddition.getDetailAddress() == null ? "" : this.memberAddition.getDetailAddress());
        return sms;
    }
}
