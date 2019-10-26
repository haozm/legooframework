package com.legooframework.model.families.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FamilyBO {

	private String id;
	
	private String name;
	// 电话
	private String phone;
	// 性别 1：男，2：女
	private Integer sex;
	// 日历类型：1 - 公历，2 - 农历
	private Integer calendarType;
	// 生日
	private String birthday;
	// 身高
	private String height;
	// 体重
	private String weight;
	// 职业
	private Integer career;
	// 是否可单独联系
	private Integer contactable;
	// 导购ID
	private Integer employeeId;
	// 导购名称
	private String employeeName;
	// 家庭成员与会员的关系
	private Integer membership;
	// 会员对家庭成员的称谓
	private String appellation;

	public FamilyBO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Integer getCalendarType() {
		return calendarType;
	}

	public void setCalendarType(Integer calendarType) {
		this.calendarType = calendarType;
	}

	public String getBirthday() {
		return this.birthday;
	}
	
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public Integer getCareer() {
		return career;
	}

	public void setCareer(Integer career) {
		this.career = career;
	}

	public Integer getContactable() {
		return contactable;
	}

	public void setContactable(Integer contactable) {
		this.contactable = contactable;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getMembership() {
		return membership;
	}

	public void setMembership(Integer membership) {
		this.membership = membership;
	}

	public String getAppellation() {
		return appellation;
	}

	public void setAppellation(String appellation) {
		this.appellation = appellation;
	}

	@Override
	public String toString() {
		return String.format(
				"FamilyBO [id=%s, name=%s, phone=%s, sex=%s, calendarType=%s, birthday=%s, height=%s, weight=%s, career=%s, contactable=%s, employeeId=%s, membership=%s, appellation=%s]",
				id, name, phone, sex, calendarType, birthday, height, weight, career, contactable, employeeId,
				membership, appellation);
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	
}
