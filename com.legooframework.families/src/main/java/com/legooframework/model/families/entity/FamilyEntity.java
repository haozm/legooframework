package com.legooframework.model.families.entity;

import java.util.Date;

import java.util.Map;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class FamilyEntity extends BaseEntity<String>{
	// 姓名
	private String name;
	// 电话
	private String phone;
	// 性别 1：男，2：女
	private Integer sex;
	// 日历类型：1 - 公历，2 - 农历
	private Integer calendarType;
	// 生日
	private Date birthday;
	// 身高
	private String height;
	// 体重
	private String weight;
	// 职业
	private Integer career;
	// 是否可单独联系
	private boolean contactable;
	// 导购ID
	private Integer employeeId;
	// 门店ID
	private Integer storeId;
	// 公司ID
	private Integer companyId;

	private FamilyEntity(String id, String name, String phone, Integer sex, Integer calendarType, Date birthday,
			String height, String weight, Integer career, boolean contactable, Integer employeeId, Integer storeId,
			Integer companyId) {
		super(id);
		this.name = name;
		this.phone = phone;
		this.sex = sex;
		this.calendarType = calendarType;
		this.birthday = birthday;
		this.height = height;
		this.weight = weight;
		this.career = career;
		this.contactable = contactable;
		this.employeeId = employeeId;
		this.storeId = storeId;
		this.companyId = companyId;
	}

	private static void checkStore(StoreEntity store) {
		Preconditions.checkNotNull(store);
		Preconditions.checkArgument(store.getCompanyId().isPresent(), String.format("门店【%s】无公司信息", store.getId()));
	}

	// 创建门店的家庭成员
	public static FamilyEntity create(String name, String phone, Integer sex, Integer calendarType, Date birthday,
			String height, String weight, Integer career, boolean contactable, EmployeeEntity employee,
			StoreEntity store) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "家庭成员姓名【name】不能为空");
		checkStore(store);
		return new FamilyEntity(generateId(), name, phone, sex, calendarType, birthday, height, weight, career,
				contactable, null == employee ? null : employee.getId(), store.getId(), store.getCompanyId().get());
	}

	/**
	 * 从数据库中还原家庭成员
	 * 
	 * @param id
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
	 * @param storeId
	 * @param companyId
	 * @param labelIds
	 * @param remarkIds
	 * @return
	 */
	static FamilyEntity valueOf(String id, String name, String phone, Integer sex, Integer calendarType,
			Date birthday, String height, String weight, Integer career, Integer contactable, Integer employeeId,
			Integer storeId, Integer companyId) {
		return new FamilyEntity(id, name, phone, sex, calendarType, birthday, height, weight, career, contactable == 1,
				employeeId, storeId, companyId);
	}

	// 是否有导购信息
	public boolean hasEmployee() {
		return null != this.employeeId;
	}

	// 是否单独联系
	public boolean isContactable() {
		return this.contactable;
	}

	// 修改家庭成员信息
	public Optional<FamilyEntity> modify(Optional<String> nameOpt, Optional<String> phoneOpt, Optional<Integer> sexOpt,
			Optional<Integer> calendarTypeOpt, Optional<Date> birthdayOpt, Optional<String> heightOpt,
			Optional<String> weightOpt, Optional<Integer> careerOpt, Optional<Integer> contactableOpt,
			Optional<EmployeeEntity> employeeOpt) {
		FamilyEntity clone = null;
		try {
			clone = (FamilyEntity) this.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("复制家庭成员信息发生异常");
		}
		Preconditions.checkState(null != clone, "复制家庭成员信息发生异常");

		if (nameOpt.isPresent())
			clone.name = nameOpt.get();
		if (phoneOpt.isPresent())
			clone.phone = phoneOpt.get();
		if (sexOpt.isPresent())
			clone.sex = sexOpt.get();
		if (calendarTypeOpt.isPresent())
			clone.calendarType = calendarTypeOpt.get();
		if (birthdayOpt.isPresent())
			clone.birthday = birthdayOpt.get();
		if (heightOpt.isPresent())
			clone.height = heightOpt.get();
		if (weightOpt.isPresent())
			clone.weight = weightOpt.get();
		if (careerOpt.isPresent())
			clone.career = careerOpt.get();
		if (contactableOpt.isPresent())
			clone.contactable = contactableOpt.get() == 1;
		if (employeeOpt.isPresent())
			clone.employeeId = employeeOpt.get().getId();

		if (clone.equalsModifyInfo(this))
			return Optional.absent();
		return Optional.of(clone);
	}

	// 修改家庭成员信息
	public Optional<FamilyEntity> modify(String name, String phone, Integer sex,
			Integer calendarType, Date birthday, String height,
			String weight, Integer career, Integer contactable,
			EmployeeEntity employee) {
		FamilyEntity clone = null;
		try {
			clone = (FamilyEntity) this.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("复制家庭成员信息发生异常");
		}
		Preconditions.checkState(null != clone, "复制家庭成员信息发生异常");
			clone.name = name;
			clone.phone = phone;
			clone.sex = sex;
			clone.calendarType = calendarType;
			clone.birthday = birthday;
			clone.height = height;
			clone.weight = weight;
			clone.career = career;
			clone.contactable = contactable == 1;
			clone.employeeId = null == employee?null:employee.getId();
		if (clone.equalsModifyInfo(this))
			return Optional.absent();
		return Optional.of(clone);
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public Integer getSex() {
		return sex;
	}

	public Integer getCalendarType() {
		return calendarType;
	}

	public Date getBirthday() {
		return birthday;
	}

	public String getHeight() {
		return height;
	}

	public String getWeight() {
		return weight;
	}

	public Integer getCareer() {
		return career;
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCreator(LoginUserContext loginUser) {
		super.setCreateUserId(loginUser.getUserId());
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();
		map.put("name", name);
		map.put("phone", phone);
		map.put("sex", sex);
		map.put("calendarType", calendarType);
		map.put("birthday", birthday);
		map.put("height", height);
		map.put("weight", weight);
		map.put("career", career);
		map.put("contactable", contactable);
		map.put("employeeId", employeeId);
		map.put("storeId", storeId);
		map.put("companyId", companyId);
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((birthday == null) ? 0 : birthday.hashCode());
		result = prime * result + ((calendarType == null) ? 0 : calendarType.hashCode());
		result = prime * result + ((career == null) ? 0 : career.hashCode());
		result = prime * result + ((companyId == null) ? 0 : companyId.hashCode());
		result = prime * result + (contactable ? 1231 : 1237);
		result = prime * result + ((employeeId == null) ? 0 : employeeId.hashCode());
		result = prime * result + ((height == null) ? 0 : height.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result + ((sex == null) ? 0 : sex.hashCode());
		result = prime * result + ((storeId == null) ? 0 : storeId.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
		return result;
	}

	// 判断待修改信息是否相同
	public boolean equalsModifyInfo(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof FamilyEntity))
			return false;
		FamilyEntity other = (FamilyEntity) obj;
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
		if (career == null) {
			if (other.career != null)
				return false;
		} else if (!career.equals(other.career))
			return false;
		if (contactable != other.contactable)
			return false;
		if (employeeId == null) {
			if (other.employeeId != null)
				return false;
		} else if (!employeeId.equals(other.employeeId))
			return false;
		if (height == null) {
			if (other.height != null)
				return false;
		} else if (!height.equals(other.height))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (sex == null) {
			if (other.sex != null)
				return false;
		} else if (!sex.equals(other.sex))
			return false;
		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof FamilyEntity))
			return false;
		FamilyEntity other = (FamilyEntity) obj;
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
		if (career == null) {
			if (other.career != null)
				return false;
		} else if (!career.equals(other.career))
			return false;
		if (companyId == null) {
			if (other.companyId != null)
				return false;
		} else if (!companyId.equals(other.companyId))
			return false;
		if (contactable != other.contactable)
			return false;
		if (employeeId == null) {
			if (other.employeeId != null)
				return false;
		} else if (!employeeId.equals(other.employeeId))
			return false;
		if (height == null) {
			if (other.height != null)
				return false;
		} else if (!height.equals(other.height))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (sex == null) {
			if (other.sex != null)
				return false;
		} else if (!sex.equals(other.sex))
			return false;
		if (storeId == null) {
			if (other.storeId != null)
				return false;
		} else if (!storeId.equals(other.storeId))
			return false;
		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"FamilyEntity [name=%s, phone=%s, sex=%s, calendarType=%s, birthday=%s, height=%s, weight=%s, career=%s, contactable=%s, employeeId=%s, storeId=%s, companyId=%s]",
				name, phone, sex, calendarType, birthday, height, weight, career, contactable, employeeId, storeId,
				companyId);
	}

}
