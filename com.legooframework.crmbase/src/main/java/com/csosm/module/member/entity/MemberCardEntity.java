package com.csosm.module.member.entity;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.collect.Maps;

public class MemberCardEntity extends BaseEntity<Integer> {

	protected MemberCardEntity(MemberEntity member) {
		super(member.getId());
		if(member.getStoreId().isPresent())
			this.createStoreId = member.getStoreId().get();
	}

	// 会员卡类型
	private Integer memberCardType;

	// 会员卡号码
	private String memberCardNum;

	// 开发门店
	private Integer createStoreId;

	// 开卡时间
	private Date createCardTime;

	// 会员卡限定时间
	private Integer limitday;
	
	//会员总积分
	private Integer totalScore;
	/**
	 * 修改会员卡
	 * 
	 * @param memberCardType
	 * @param memberCardNum
	 * @param createCardTime
	 * @return
	 */
	public MemberCardEntity modify(Integer memberCardType, String memberCardNum) {
		MemberCardEntity clone = null;
		try {
			clone = (MemberCardEntity) this.clone();
			clone.memberCardNum = memberCardNum;
			clone.memberCardType = memberCardType;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("克隆会员卡信息发生异常", e);
		}
		return clone;
	}
	
	public MemberCardEntity modify(Optional<Integer> memberCardTypeOpt, Optional<String> memberCardNumOpt,Optional<Date> createCardTimeOpt) {
		MemberCardEntity clone = null;
		try {
			clone = (MemberCardEntity) this.clone();
			if(memberCardNumOpt.isPresent()) clone.memberCardNum = memberCardNumOpt.get();
			if(memberCardTypeOpt.isPresent()) clone.memberCardType = memberCardTypeOpt.get();
			if(createCardTimeOpt.isPresent()) clone.createCardTime = createCardTimeOpt.get();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("克隆会员卡信息发生异常", e);
		}
		return clone;
	}

	protected MemberCardEntity(Integer id, Integer memberCardType, String memberCardNum, Integer createStoreId,
			Date createCardTime, Integer limitday,Integer totalScore) {
		super(id);
		this.memberCardType = memberCardType;
		this.memberCardNum = memberCardNum;
		this.createStoreId = createStoreId;
		this.createCardTime = createCardTime;
		this.limitday = limitday;
	}

	protected static MemberCardEntity valueOf(Integer id, Integer memberCardType, String memberCardNum,
			Integer createStoreId, Date createCardTime, Integer limitday,Integer totalScore) {
		return new MemberCardEntity(id, memberCardType, memberCardNum, createStoreId, createCardTime, limitday,totalScore);
	}
	
	

	public Integer getMemberCardType() {
		return memberCardType;
	}

	public String getMemberCardNum() {
		return memberCardNum;
	}

	public Integer getCreateStoreId() {
		return createStoreId;
	}

	public Date getCreateCardTime() {
		return createCardTime;
	}

	public Integer getLimitday() {
		return limitday;
	}

	public Integer getTotalScore() {
		return totalScore;
	}

	public Map<String, Object> toStorageMap() {
		Map<String,Object> map = Maps.newHashMap();
		map.put("memberId", this.getId());
		map.put("memberCardType", this.memberCardType);
		map.put("memberCardNum", this.memberCardNum);
		map.put("createStoreId", this.createStoreId);
		map.put("createCardTime", this.createCardTime);
		map.put("limitday", this.limitday);
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.getId().hashCode();
		result = prime * result + ((createCardTime == null) ? 0 : createCardTime.hashCode());
		result = prime * result + ((createStoreId == null) ? 0 : createStoreId.hashCode());
		result = prime * result + ((limitday == null) ? 0 : limitday.hashCode());
		result = prime * result + ((memberCardNum == null) ? 0 : memberCardNum.hashCode());
		result = prime * result + ((memberCardType == null) ? 0 : memberCardType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		MemberCardEntity other = (MemberCardEntity) obj;
		if (this.getId() != other.getId())
			return false;
		if (createCardTime == null) {
			if (other.createCardTime != null)
				return false;
		} else if (!createCardTime.equals(other.createCardTime))
			return false;
		if (createStoreId == null) {
			if (other.createStoreId != null)
				return false;
		} else if (!createStoreId.equals(other.createStoreId))
			return false;
		if (limitday == null) {
			if (other.limitday != null)
				return false;
		} else if (!limitday.equals(other.limitday))
			return false;
		if (memberCardNum == null) {
			if (other.memberCardNum != null)
				return false;
		} else if (!memberCardNum.equals(other.memberCardNum))
			return false;
		if (memberCardType == null) {
			if (other.memberCardType != null)
				return false;
		} else if (!memberCardType.equals(other.memberCardType))
			return false;
		return true;
	}
	
}
