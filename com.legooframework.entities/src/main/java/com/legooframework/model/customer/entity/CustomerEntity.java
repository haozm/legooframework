package com.legooframework.model.customer.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.core.jdbc.BatchSetter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CustomerEntity extends BaseEntity<CustomerId> implements BatchSetter {
	// /**
	// * 顾客唯一标识
	// */
	// private CustomerId cid;
	/**
	 * 生日
	 */
	private Date birthday;
	/**
	 * 农历生日
	 */
	private int birthdayLunar;
	/**
	 * 星座、农历日期，生效、名称、名称短拼、备注、备注短拼、性别
	 */
	private String constellation, lunarDates, zodiac, name, nameShort, remark, remarkShort;
	/**
	 * 性别
	 */
	private KvDictDto sex;
	/**
	 * 证件类型
	 */
	private KvDictDto certType;
	/**
	 * 证件号码
	 */
	private String idNumber;
	/**
	 * 职业
	 */
	private KvDictDto career;
	/**
	 * 婚姻情况
	 */
	private KvDictDto marriage;
	/**
	 * 教育程度
	 */
	private KvDictDto education;
	/**
	 * 个性
	 */
	private KvDictDto disposition;
	/**
	 * 宗教信仰
	 */
	private KvDictDto religion;
	/**
	 * 在乎的人、崇拜的人、图像
	 */
	private String heedUser, admireUser, iconUrl;

	CustomerEntity(CustomerId cid, String name, Date birthday, int birthdayLunar, String lunarDates, String zodiac,
			String constellation, String remark, KvDictDto sex, KvDictDto certType, String idNumber, KvDictDto career,
			KvDictDto marriage, KvDictDto education, String iconUrl, KvDictDto disposition, KvDictDto religion,
			String heedUser, String admireUser, LoginContext lc) {
		super(cid, lc.getTenantId(), lc.getLoginId());
		this.name = name;
		this.birthday = birthday;
		this.sex = sex;
		this.certType = certType;
		this.idNumber = idNumber;
		this.zodiac = zodiac;
		this.constellation = constellation;
		this.remark = remark;
		this.career = career;
		this.marriage = marriage;
		this.education = education;
		this.iconUrl = iconUrl;
		this.disposition = disposition;
		this.religion = religion;
		this.heedUser = heedUser;
		this.admireUser = admireUser;
	}

	CustomerEntity(CustomerId cid, String name, String remark, String iconUrl, KvDictDto sex, LoginContext lc) {
		super(cid, lc.getTenantId(), lc.getLoginId());
		this.name = name;
		this.remark = remark;
		this.iconUrl = iconUrl;
		this.sex = sex;
	}

	CustomerEntity(CustomerId cid, ResultSet res) throws SQLException {
		super(cid, res);
		this.name = res.getString("name");
		this.birthday = res.getDate("birthday");
		this.birthdayLunar = res.getInt("birthdayLunar");
		this.lunarDates = res.getString("lunarDates");
		this.constellation = res.getString("constellation");
		String sexRes = res.getString("sex");
		this.sex = sexRes == null ? null : new KvDictDto(sexRes);
		this.zodiac = res.getString("zodiac");
		String marriageRes = res.getString("marriage");
		this.marriage = marriageRes == null ? null : new KvDictDto(marriageRes);
		String certTypeRes = res.getString("certType");
		this.certType = certTypeRes == null ? null : new KvDictDto(certTypeRes);
		this.idNumber = res.getString("idNumber");
		String careerRes = res.getString("career");
		this.career = careerRes == null ? null : new KvDictDto(careerRes);
		String educationRes = res.getString("education");
		this.education = educationRes == null ? null : new KvDictDto(educationRes);
		this.iconUrl = res.getString("iconUrl");
		String dispositionRes = res.getString("disposition");
		this.disposition = dispositionRes == null ? null : new KvDictDto(dispositionRes);
		String religionRes = res.getString("religion");
		this.religion = religionRes == null ? null : new KvDictDto(religionRes);
		this.heedUser = res.getString("heedUser");
		this.admireUser = res.getString("admireUser");
		this.nameShort = res.getString("nameShort");
		this.remark = res.getString("remark");
		this.remarkShort = res.getString("remarkShort");
	}

	protected CustomerEntity() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	CustomerEntity(CustomerId cid, String name, String remark, String iconUrl, KvDictDto sex, Long tenantId,
			LoginContext lc) {
		super(cid, tenantId, lc.getLoginId());
		this.name = name;
		this.remark = remark;
		this.iconUrl = iconUrl;
		this.sex = sex;
	}

	/**
	 * 更改已改变的
	 *
	 * @param name
	 * @param remark
	 * @param birthday
	 * @param birthdayLunar
	 * @param lunarDates
	 * @param zodiac
	 * @param constellation
	 * @param sex
	 * @param certType
	 * @param idNumber
	 * @param career
	 * @param marriage
	 * @param education
	 * @param disposition
	 * @param religion
	 * @param heedUser
	 * @param admireUser
	 * @param iconUrl
	 * @return
	 */
	public Optional<CustomerEntity> modifyVaried(String name, Date birthday, int birthdayLunar, String lunarDates,
			String zodiac, String constellation, String remark, KvDictDto sex, KvDictDto certType, String idNumber,
			KvDictDto career, KvDictDto marriage, KvDictDto education, KvDictDto disposition, KvDictDto religion,
			String heedUser, String admireUser, String iconUrl) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "名称不能为空");
		Objects.requireNonNull(sex, "性别不能为空");
		CustomerEntity clone = (CustomerEntity) this.cloneMe();
		clone.name = name;
		clone.birthday = birthday;
		clone.sex = sex;
		clone.certType = certType;
		clone.idNumber = idNumber;
		clone.zodiac = zodiac;
		clone.constellation = constellation;
		clone.remark = remark;
		clone.career = career;
		clone.marriage = marriage;
		clone.education = education;
		clone.iconUrl = iconUrl;
		clone.disposition = disposition;
		clone.religion = religion;
		clone.heedUser = heedUser;
		clone.admireUser = admireUser;
		if (clone.equals(this))
			return Optional.empty();
		return Optional.of(clone);
	}

	public Optional<CustomerEntity> modifyVaried(String name, Date birthday, KvDictDto sex, int lunarBirthday,
			String zodiac, String lunarDates) {

		Objects.requireNonNull(sex, "性别不能为空");
		CustomerEntity clone = (CustomerEntity) this.cloneMe();
		clone.name = name;
		clone.birthday = birthday;
		clone.birthdayLunar = lunarBirthday;
		clone.zodiac = zodiac;
		clone.lunarDates = lunarDates;
		clone.sex = sex;
		if (clone.equals(this))
			return Optional.empty();
		return Optional.of(clone);
	}

	public CustomerEntity changer() {
		CustomerEntity clone = (CustomerEntity) this.cloneMe();
		return clone;
	}

	public CustomerEntity modifyName(String name) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "名称不能为空");
		this.name = name;
		return this;
	}

	public CustomerEntity modifyBirthDay(Date birthday, int lunarBirthday, String zodiac, String lunarDates) {
		Objects.requireNonNull(birthday, "生日不能为空");
		this.birthday = birthday;
		this.birthday = birthday;
		this.birthdayLunar = lunarBirthday;
		this.zodiac = zodiac;
		this.lunarDates = lunarDates;
		return this;
	}

	public CustomerEntity modifySex(KvDictDto sex) {
		Objects.requireNonNull(sex, "性别不能为空");
		this.sex = sex;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((admireUser == null) ? 0 : admireUser.hashCode());
		result = prime * result + ((birthday == null) ? 0 : birthday.hashCode());
		result = prime * result + birthdayLunar;
		result = prime * result + ((career == null) ? 0 : career.hashCode());
		result = prime * result + ((certType == null) ? 0 : certType.hashCode());
		result = prime * result + ((constellation == null) ? 0 : constellation.hashCode());
		result = prime * result + ((disposition == null) ? 0 : disposition.hashCode());
		result = prime * result + ((education == null) ? 0 : education.hashCode());
		result = prime * result + ((heedUser == null) ? 0 : heedUser.hashCode());
		result = prime * result + ((iconUrl == null) ? 0 : iconUrl.hashCode());
		result = prime * result + ((idNumber == null) ? 0 : idNumber.hashCode());
		result = prime * result + ((lunarDates == null) ? 0 : lunarDates.hashCode());
		result = prime * result + ((marriage == null) ? 0 : marriage.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nameShort == null) ? 0 : nameShort.hashCode());
		result = prime * result + ((religion == null) ? 0 : religion.hashCode());
		result = prime * result + ((remark == null) ? 0 : remark.hashCode());
		result = prime * result + ((remarkShort == null) ? 0 : remarkShort.hashCode());
		result = prime * result + ((sex == null) ? 0 : sex.hashCode());
		result = prime * result + ((zodiac == null) ? 0 : zodiac.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomerEntity other = (CustomerEntity) obj;
		if (admireUser == null) {
			if (other.admireUser != null)
				return false;
		} else if (!admireUser.equals(other.admireUser))
			return false;
		if (birthday == null) {
			if (other.birthday != null)
				return false;
		} else if (!birthday.equals(other.birthday))
			return false;
		if (birthdayLunar != other.birthdayLunar)
			return false;
		if (career == null) {
			if (other.career != null)
				return false;
		} else if (!career.equals(other.career))
			return false;
		if (certType == null) {
			if (other.certType != null)
				return false;
		} else if (!certType.equals(other.certType))
			return false;
		if (constellation == null) {
			if (other.constellation != null)
				return false;
		} else if (!constellation.equals(other.constellation))
			return false;
		if (disposition == null) {
			if (other.disposition != null)
				return false;
		} else if (!disposition.equals(other.disposition))
			return false;
		if (education == null) {
			if (other.education != null)
				return false;
		} else if (!education.equals(other.education))
			return false;
		if (heedUser == null) {
			if (other.heedUser != null)
				return false;
		} else if (!heedUser.equals(other.heedUser))
			return false;
		if (iconUrl == null) {
			if (other.iconUrl != null)
				return false;
		} else if (!iconUrl.equals(other.iconUrl))
			return false;
		if (idNumber == null) {
			if (other.idNumber != null)
				return false;
		} else if (!idNumber.equals(other.idNumber))
			return false;
		if (lunarDates == null) {
			if (other.lunarDates != null)
				return false;
		} else if (!lunarDates.equals(other.lunarDates))
			return false;
		if (marriage == null) {
			if (other.marriage != null)
				return false;
		} else if (!marriage.equals(other.marriage))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nameShort == null) {
			if (other.nameShort != null)
				return false;
		} else if (!nameShort.equals(other.nameShort))
			return false;
		if (religion == null) {
			if (other.religion != null)
				return false;
		} else if (!religion.equals(other.religion))
			return false;
		if (remark == null) {
			if (other.remark != null)
				return false;
		} else if (!remark.equals(other.remark))
			return false;
		if (remarkShort == null) {
			if (other.remarkShort != null)
				return false;
		} else if (!remarkShort.equals(other.remarkShort))
			return false;
		if (sex == null) {
			if (other.sex != null)
				return false;
		} else if (!sex.equals(other.sex))
			return false;
		if (zodiac == null) {
			if (other.zodiac != null)
				return false;
		} else if (!zodiac.equals(other.zodiac))
			return false;
		return true;
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		// REPLACE INTO csosm_chat.customer_base_info
		// ( id, account_type,store_id, birthday, constellation, birthday_lunar,
		// lunar_map_dates,
		// user_zodiac, user_name, user_sex, cert_type, id_number, user_career,
		// user_marriage,
		// user_education, user_disposition, user_religion, heed_user, admire_user,
		// user_icon, user_remark,
		// tenant_id, creator, createTime,editor,editTime)
		// VALUES ( ?, ?, ?, ?, ?, ?, ?,
		// ?, ?, ?, ?, ?, ?, ?,
		// ?, ?, ?, ?, ?, ?, ?,
		// ?, ?, ?, ?, NOW())
		ps.setObject(1, this.getId().getId());
		ps.setObject(2, this.getId().getChannel().getVal());
		ps.setObject(3, this.getId().getStoreId());
		ps.setObject(4, getNullable(this.getBirthday()));
		ps.setObject(5, getNullable(this.getConstellation()));
		ps.setObject(6, getNullable(this.getBirthdayLunar()));
		ps.setObject(7, getNullable(this.getLunarDates()));
		ps.setObject(8, getNullable(this.getZodiac()));
		ps.setObject(9, this.getName());
		ps.setObject(10, getNullableDict(this.getSex()));
		ps.setObject(11, getNullableDict(this.getCertType()));
		ps.setObject(12, getNullable(this.getIdNumber()));
		ps.setObject(13, getNullableDict(this.getCareer()));
		ps.setObject(14, getNullableDict(this.getMarriage()));
		ps.setObject(15, getNullableDict(this.getEducation()));
		ps.setObject(16, getNullableDict(this.getDisposition()));
		ps.setObject(17, getNullableDict(this.getReligion()));
		ps.setObject(18, getNullable(this.getHeedUser()));
		ps.setObject(19, getNullable(this.getAdmireUser()));
		ps.setObject(20, getNullable(this.getIconUrl()));
		ps.setObject(21, getNullable(this.getRemark()));
		ps.setObject(22, this.getTenantId());
		ps.setObject(23, this.getCreator());
		ps.setObject(24, new Date());
		ps.setObject(25, this.getEditor().isPresent() ? this.getEditor().get() : null);
	}

	private Object getNullable(Object res) {
		return res == null ? null : res;
	}

	private String getNullableDict(Optional<KvDictDto> res) {
		return !res.isPresent() ? null : res.get().getValue();
	}

	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Object> paramMap = super.toParamMap(excludes);
		paramMap.put("id", this.getId().getId());
		paramMap.put("channel", this.getId().getChannel().getVal());
		paramMap.put("storeId", this.getId().getStoreId());
		paramMap.put("name", this.getName());
		paramMap.put("birthday", this.getBirthday() == null ? null : dateFormat.format(this.getBirthday()));
		paramMap.put("birthdayLunar", this.getBirthdayLunar());
		paramMap.put("lunarDates", this.getLunarDates());
		paramMap.put("zodiac", this.getZodiac());
		paramMap.put("constellation", this.getConstellation());
		paramMap.put("sex", !this.getSex().isPresent() ? null : this.getSex().get().getValue());
		paramMap.put("certType", !this.getCertType().isPresent() ? null : this.getCertType().get().getValue());
		paramMap.put("idNumber", this.getIdNumber());
		paramMap.put("career", !this.getCareer().isPresent() ? null : this.getCareer().get().getValue());
		paramMap.put("marriage", !this.getMarriage().isPresent() ? null : this.getMarriage().get().getValue());
		paramMap.put("education", !this.getEducation().isPresent() ? null : this.getEducation().get().getValue());
		paramMap.put("iconUrl", this.getIconUrl());
		paramMap.put("disposition", !this.getDisposition().isPresent() ? null : this.getDisposition().get().getValue());
		paramMap.put("religion", !this.getReligion().isPresent() ? null : this.getReligion().get().getValue());
		paramMap.put("heedUser", this.getHeedUser());
		paramMap.put("admireUser", this.getAdmireUser());
		paramMap.put("remark", this.getRemark());
		return paramMap;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public boolean hasWechatAccount() {
		return this.getId().getChannel() == Channel.TYPE_WEIXIN;
	}

	public boolean hasMemberAccount() {
		return this.getId().getChannel() == Channel.TYPE_MEMBER;
	}

	public boolean hasPublicAccount() {
		// TODO
		return false;
	}

	public Date getBirthday() {
		return birthday;
	}

	public int getBirthdayLunar() {
		return birthdayLunar;
	}

	public String getConstellation() {
		return constellation;
	}

	public String getLunarDates() {
		return lunarDates;
	}

	public String getZodiac() {
		return zodiac;
	}

	public String getName() {
		return name;
	}

	public String getNameShort() {
		return nameShort;
	}

	public String getRemark() {
		return remark;
	}

	public String getRemarkShort() {
		return remarkShort;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public String getHeedUser() {
		return heedUser;
	}

	public String getAdmireUser() {
		return admireUser;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public Optional<KvDictDto> getSex() {
		return sex == null ? Optional.empty() : Optional.of(this.sex);
	}

	public Optional<KvDictDto> getCertType() {
		return certType == null ? Optional.empty() : Optional.of(this.certType);
	}

	public Optional<KvDictDto> getCareer() {
		return career == null ? Optional.empty() : Optional.of(this.career);
	}

	public Optional<KvDictDto> getMarriage() {
		return marriage == null ? Optional.empty() : Optional.of(this.marriage);
	}

	public Optional<KvDictDto> getEducation() {
		return education == null ? Optional.empty() : Optional.of(education);
	}

	public Optional<KvDictDto> getDisposition() {
		return disposition == null ? Optional.empty() : Optional.of(disposition);
	}

	public Optional<KvDictDto> getReligion() {
		return religion == null ? Optional.empty() : Optional.of(religion);
	}

}
