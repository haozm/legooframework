package com.csosm.module.webchat.entity;

import java.util.Date;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.google.common.base.Preconditions;

public class WechatAddFriendListEntity extends BaseEntity<Long> {

    private final Integer memberId, storeId, companyId;
    private String memberName, phoneNo;
    // 0 生成名单  1、下发指令   2、推送移动端成功
    private int pushFlag;
    private Date joinDate, pushDate;
    private String weixinId;
    private final int pushType;
    private int runtimes;

    WechatAddFriendListEntity(Object userId, Date createTime, MemberEntity member, int pushType, int runtimes) {
        super(0L, userId, createTime);
        Preconditions.checkState(member.getStoreId().isPresent());
        this.memberId = member.getId();
        this.storeId = member.getStoreId().get();
        this.companyId = member.getCompanyId();
        this.memberName = member.getName();
        this.phoneNo = member.getMobilephone();
        this.pushFlag = 0;
        this.pushType = pushType;
        this.runtimes = runtimes;
    }

    WechatAddFriendListEntity(Long id, Integer memberId, Integer storeId, Integer companyId, String memberName,
                              String phoneNo, int pushFlag, Date joinDate, Date pushDate, String weixinId, int pushType,
                              int runtimes) {
        super(id);
        this.memberId = memberId;
        this.storeId = storeId;
        this.companyId = companyId;
        this.memberName = memberName;
        this.phoneNo = phoneNo;
        this.pushFlag = pushFlag;
        this.joinDate = joinDate;
        this.pushDate = pushDate;
        this.weixinId = weixinId;
        this.pushType = pushType;
        this.runtimes = runtimes;
    }

	public Integer getMemberId() {
		return memberId;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public String getMemberName() {
		return memberName;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public int getPushFlag() {
		return pushFlag;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public Date getPushDate() {
		return pushDate;
	}

	public String getWeixinId() {
		return weixinId;
	}

	public int getPushType() {
		return pushType;
	}

	public int getRuntimes() {
		return runtimes;
	}
    
    
}
