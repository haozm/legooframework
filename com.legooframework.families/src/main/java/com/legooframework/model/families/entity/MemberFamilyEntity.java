package com.legooframework.model.families.entity;

import java.util.Map;



import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class MemberFamilyEntity extends BaseEntity<Integer>{
	// 会员ID
	private Integer memberId;
	// 家庭成员
	private FamilyEntity family;
	// 家庭成员与会员的关系
	private Integer membership;
	// 会员对家庭成员的称谓
	private String appellation;
	//门店ID
	private Integer storeId;
	//公司ID
	private Integer companyId;

	private MemberFamilyEntity(Integer id, Integer memberId, FamilyEntity family, Integer membership,
			String appellation) {
		super(id);
		this.memberId = memberId;
		this.family = family;
		this.membership = membership;
		this.appellation = appellation;
		this.storeId = family.getStoreId();
		this.companyId = family.getCompanyId();
	}

	// 新建会员家庭成员
	public static MemberFamilyEntity create(MemberEntity member, Integer membership, String appellation,FamilyEntity family) {
		Preconditions.checkNotNull(member);
		return new MemberFamilyEntity(null, member.getId(), family, membership, appellation);
	}
	
	//从数据库还原会员家庭成员
	public static MemberFamilyEntity valueOf(Integer id, Integer memberId, FamilyEntity family, Integer membership,
			String appellation) {
		return new MemberFamilyEntity(id, memberId, family, membership, appellation);
	}	
	/**
	 * 修改会员信息
	 * @param memberOpt
	 * @param membershipOpt
	 * @param appellationOpt
	 * @return
	 */
	public Optional<MemberFamilyEntity> modify(Optional<MemberEntity> memberOpt, Optional<Integer> membershipOpt,
			Optional<String> appellationOpt) {
		MemberFamilyEntity clone = null;
		try {
			clone = (MemberFamilyEntity) this.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("复制会员家庭成员信息异常");
		}
		Preconditions.checkState(null != clone, "复制会员家庭成员信息异常");
		if (memberOpt.isPresent())
			clone.memberId = memberOpt.get().getId();
		if (membershipOpt.isPresent())
			clone.membership = membershipOpt.get();
		if (appellationOpt.isPresent())
			clone.appellation = appellationOpt.get();
		if (clone.equalsModifyInfo(this)) return Optional.absent();
		return Optional.of(clone);
	}
	
	public Optional<MemberFamilyEntity> modify(MemberEntity member, Integer membership, String appellation) {
		MemberFamilyEntity clone = null;
		try {
			clone = (MemberFamilyEntity) this.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("复制会员家庭成员信息异常");
		}
		Preconditions.checkState(null != clone, "复制会员家庭成员信息异常");
		clone.memberId = member.getId();
		clone.membership = membership;
		clone.appellation = appellation;
		if (clone.equalsModifyInfo(this)) return Optional.absent();
		return Optional.of(clone);
	}
	
	

	public boolean equalsModifyInfo(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof MemberFamilyEntity))
			return false;
		MemberFamilyEntity other = (MemberFamilyEntity) obj;
		if (appellation == null) {
			if (other.appellation != null)
				return false;
		} else if (!appellation.equals(other.appellation))
			return false;
		if (memberId == null) {
			if (other.memberId != null)
				return false;
		} else if (!memberId.equals(other.memberId))
			return false;
		if (membership == null) {
			if (other.membership != null)
				return false;
		} else if (!membership.equals(other.membership))
			return false;
		return true;
	}
	
	//是否已绑定会员
	public boolean hasBindMember() {
		return null != this.memberId;
	}
	
	public void setCreator(LoginUserContext loginUser) {
		super.setCreateUserId(loginUser.getUserId());
	}

	public Integer getMemberId() {
		return memberId;
	}

	public FamilyEntity getFamily() {
		return family;
	}

	public Integer getMembership() {
		return membership;
	}

	public String getAppellation() {
		return appellation;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}
	
	public Map<String,Object> toMap() {
		Map<String, Object> map = this.family.toMap();
		map.put("memberFamilyId", this.getId());
		map.put("memberId", memberId);
		map.put("membership", membership);
		map.put("appellation", appellation);
		return map;
	}

	@Override
	public String toString() {
		return String.format(
				"MemberFamilyEntity [memberId=%s, family=%s, membership=%s, appellation=%s, storeId=%s, companyId=%s]",
				memberId, family, membership, appellation, storeId, companyId);
	}

	public String getPhone() {
		return this.family.getPhone();
	}

	public String getName() {
		return this.family.getName();
	}

	public Map<String, String> toSmsMap(StoreEntity store) {
		Map<String,String> smsMap = Maps.newHashMap();
		smsMap.put("成员名", this.family.getName());
		smsMap.put("成员称谓", this.appellation);
		return smsMap;
	}
	
}
