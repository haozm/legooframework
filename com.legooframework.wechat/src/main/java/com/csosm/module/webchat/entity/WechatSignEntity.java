package com.csosm.module.webchat.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.EmployeeEntity;

public class WechatSignEntity extends BaseEntity<Long> {

	public static final String SIGNED = "signed";

	public static final String UNSIGNED = "unsigned";

	// 门店ID 公司ID
	private final Integer storeId, companyId;
	// 被认领的微信ID
	private String weixinId;
	// 认领导购ID
	private Integer employeeId;

	public WechatSignEntity(EmployeeEntity employee, WebChatUserEntity weixin) {
		super(0L, employee.getId(), new Date());
		this.storeId = employee.getStoreId().get();
		this.companyId = employee.getCompanyId().get();
		this.weixinId = weixin.getId();
		this.employeeId = employee.getId();
	}

	private WechatSignEntity(Long id, Integer storeId, Integer companyId, String weixinId, Integer employeeId) {
		super(id);
		this.storeId = storeId;
		this.companyId = companyId;
		this.weixinId = weixinId;
		this.employeeId = employeeId;
	}

	public WechatSignEntity changeSigner(EmployeeEntity employee) {
		WechatSignEntity clone = null;
		try {
			clone = (WechatSignEntity) this.clone();
			clone.employeeId = employee.getId();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	public WechatSignEntity resign(WebChatUserEntity weixin) {
		WechatSignEntity clone = null;
		try {
			clone = (WechatSignEntity) this.clone();
			clone.weixinId = weixin.getId();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	public boolean hasSigned(WebChatUserEntity weixin) {
		return weixin.getId().equals(this.weixinId);
	}

	public boolean hasBeSigned(EmployeeEntity employee) {
		return employee.getId() == this.employeeId;
	}

	public boolean hasSigned(EmployeeEntity employee,WebChatUserEntity weixin) {
		return this.employeeId.intValue() == employee.getId().intValue() && this.weixinId.equals(weixin.getUserName());
	}
	
	static WechatSignEntity valueOf(ResultSet rs) {
		try {
			Long id = rs.getLong("id");
			Integer employeeId = rs.getInt("employeeId");
			String weixinId = rs.getString("weixinId");
			Integer storeId = rs.getInt("storeId");
			Integer companyId = rs.getInt("companyId");
			return new WechatSignEntity(id, storeId, companyId, weixinId, employeeId);
		} catch (SQLException e) {
			throw new RuntimeException("数据库还原认领信息对象发生异常");
		}

	}

	public Map<String, Object> toMap() {
		Map<String, Object> params = super.toMap();
		params.put("id", this.getId());
		params.put("employeeId", this.employeeId);
		params.put("weixinId", this.weixinId);
		params.put("storeId", this.storeId);
		params.put("companyId", this.companyId);
		return params;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public String getWeixinId() {
		return weixinId;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

}
