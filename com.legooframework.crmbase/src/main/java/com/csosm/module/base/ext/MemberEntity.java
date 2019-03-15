package com.csosm.module.base.ext;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.Birthday;

import com.csosm.module.base.entity.KvDictEntity;

public class MemberEntity extends BaseEntity<Integer> {

	protected MemberEntity(Integer id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	//会员名称
	private String name;
	//会员性别
	private int sex;
	//会员备注
	private String remark;
	//会员生日
	private Birthday birthday;
	// 1 - 身份证， 2 - 员工证件， 3 - 其它
	private KvDictEntity certificateType;
	// 证件号
	private String certificate;
	//会员详细地址
	private String detailAddress;
	//会员婚姻状况
	private KvDictEntity marryStatus;
	//会员状态
	private int status;
	
}
