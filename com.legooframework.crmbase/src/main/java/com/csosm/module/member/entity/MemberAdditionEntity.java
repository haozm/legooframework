package com.csosm.module.member.entity;

import java.util.Map;
import java.util.Optional;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.collect.Maps;

public class MemberAdditionEntity extends BaseEntity<Integer> {

	// 在乎的人
	private String carePeople;

	// 性格特征
	private Integer characterType;

	// 信仰
	private Integer faithType;

	// 业余爱好
	private String hobby;

	// 崇拜的人
	private String idols;

	// 工作职业
	private String jobType;

	// 喜欢的品牌
	private String likeBrand;

	// 最佳联系方式
	private Integer likeContact;

	// 是否已结婚
	private Integer marryStatus;

	// 特殊纪念日
	private String specialDay;

	// 星座
	private Integer zodiac;

	// 教育程度
	private Integer education;

	// 最佳联系时间
	private Integer likeContactTime;

	// 会员证件号码
	private String certificate;

	// 会员证件类型
	private Integer certificateType;

	// 会员详细地址
	private String detailAddress;

	protected MemberAdditionEntity(MemberEntity member) {
		super(member.getId());
	}

	protected MemberAdditionEntity(Integer id, String carePeople, Integer characterType, Integer faithType,
			String hobby, String idols, String jobType, String likeBrand, Integer likeContact, Integer marryStatus,
			String specialDay, Integer zodiac, Integer education, Integer likeContactTime, String certificate,
			Integer certificateType, String detailAddress) {
		super(id);
		this.carePeople = carePeople;
		this.characterType = characterType;
		this.faithType = faithType;
		this.hobby = hobby;
		this.idols = idols;
		this.jobType = jobType;
		this.likeBrand = likeBrand;
		this.likeContact = likeContact;
		this.marryStatus = marryStatus;
		this.specialDay = specialDay;
		this.zodiac = zodiac;
		this.education = education;
		this.likeContactTime = likeContactTime;
		this.certificate = certificate;
		this.certificateType = certificateType;
		this.detailAddress = detailAddress;
	}

	public MemberAdditionEntity modify(Integer marryStatus, String idols, String carePeople, Integer zodiac,
			Integer characterType, String jobType, Integer faithType, String hobby, String likeBrand,
			Integer likeContact, String specialDay, Integer education, Integer likeContactTime, String certificate,
			Integer certificateType, String detailAddress) {
		MemberAdditionEntity clone = null;
		try {
			clone = (MemberAdditionEntity) this.clone();
			clone.marryStatus = marryStatus;
			clone.idols = idols;
			clone.carePeople = carePeople;
			clone.zodiac = zodiac;
			clone.characterType = characterType;
			clone.jobType = jobType;
			clone.faithType = faithType;
			clone.hobby = hobby;
			clone.specialDay = specialDay;
			clone.education = education;
			clone.likeContactTime = likeContactTime;
			clone.certificate = certificate;
			clone.certificateType = certificateType;
			clone.likeBrand = likeBrand;
			clone.detailAddress = detailAddress;
			clone.likeContact = likeContact;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("克隆会员附加信息发生异常", e);
		}
		return clone;
	}

	// Optional<Integer> Optional<String>
	public MemberAdditionEntity modify(Optional<Integer> marryStatusOpt, Optional<String> idolsOpt,
			Optional<String> carePeopleOpt, Optional<Integer> zodiacOpt, Optional<Integer> characterTypeOpt,
			Optional<String> jobTypeOpt, Optional<Integer> faithTypeOpt, Optional<String> hobbyOpt,
			Optional<String> likeBrandOpt, Optional<Integer> likeContactOpt, Optional<String> specialDayOpt,
			Optional<Integer> educationOpt, Optional<Integer> likeContactTimeOpt, Optional<String> certificateOpt,
			Optional<Integer> certificateTypeOpt, Optional<String> detailAddressOpt) {
		MemberAdditionEntity clone = null;
		try {
			clone = (MemberAdditionEntity) this.clone();
			if(marryStatusOpt.isPresent()) clone.marryStatus = marryStatusOpt.get();
			if(idolsOpt.isPresent()) clone.idols = idolsOpt.get();
			if(carePeopleOpt.isPresent()) clone.carePeople = carePeopleOpt.get();
			if(zodiacOpt.isPresent()) clone.zodiac = zodiacOpt.get();
			if(characterTypeOpt.isPresent()) clone.characterType = characterTypeOpt.get();
			if(jobTypeOpt.isPresent()) clone.jobType = jobTypeOpt.get();
			if(faithTypeOpt.isPresent()) clone.faithType = faithTypeOpt.get();
			if(hobbyOpt.isPresent()) clone.hobby = hobbyOpt.get();
			if(specialDayOpt.isPresent()) clone.specialDay = specialDayOpt.get();
			if(educationOpt.isPresent()) clone.education = educationOpt.get();
			if(likeContactTimeOpt.isPresent()) clone.likeContactTime = likeContactTimeOpt.get();
			if(certificateOpt.isPresent()) clone.certificate = certificateOpt.get();
			if(certificateTypeOpt.isPresent()) clone.certificateType = certificateTypeOpt.get();
			if(likeBrandOpt.isPresent()) clone.likeBrand = likeBrandOpt.get();
			if(detailAddressOpt.isPresent()) clone.detailAddress = detailAddressOpt.get();
			if(likeContactOpt.isPresent()) clone.likeContact = likeContactOpt.get();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("克隆会员附加信息发生异常", e);
		}
		return clone;
	}

	protected static MemberAdditionEntity valueOf(Integer id, String carePeople, Integer characterType,
			Integer faithType, String hobby, String idols, String jobType, String likeBrand, Integer likeContact,
			Integer marryStatus, String specialDay, Integer zodiac, Integer education, Integer likeContactTime,
			String certificate, Integer certificateType, String detailAddress) {
		return new MemberAdditionEntity(id, carePeople, characterType, faithType, hobby, idols, jobType, likeBrand,
				likeContact, marryStatus, specialDay, zodiac, education, likeContactTime, certificate, certificateType,
				detailAddress);
	}

	public Map<String, Object> toStorageMap() {
		Map<String, Object> map = Maps.newHashMap();
		map.put("memberId", this.getId());
		map.put("carePeople", this.carePeople);
		map.put("characterType", this.characterType);
		map.put("faithType", this.faithType);
		map.put("hobby", this.hobby);
		map.put("idols", this.idols);
		map.put("jobType", this.jobType);
		map.put("likeBrand", this.likeBrand);
		map.put("likeContact", this.likeContact);
		map.put("marryStatus", this.marryStatus);
		map.put("specialDay", this.specialDay);
		map.put("zodiac", this.zodiac);
		map.put("education", this.education);
		map.put("likeContactTime", this.likeContactTime);
		map.put("detailAddress", this.detailAddress);
		map.put("certificate", this.certificate);
		map.put("certificateType", this.certificateType);
		return map;
	}

	public String getCarePeople() {
		return carePeople;
	}

	public Integer getCharacterType() {
		return characterType;
	}

	public Integer getFaithType() {
		return faithType;
	}

	public String getHobby() {
		return hobby;
	}

	public String getIdols() {
		return idols;
	}

	public String getJobType() {
		return jobType;
	}

	public String getLikeBrand() {
		return likeBrand;
	}

	public Integer getLikeContact() {
		return likeContact;
	}

	public Integer getMarryStatus() {
		return marryStatus;
	}

	public String getSpecialDay() {
		return specialDay;
	}

	public Integer getZodiac() {
		return zodiac;
	}

	public Integer getEducation() {
		return education;
	}

	public Integer getLikeContactTime() {
		return likeContactTime;
	}

	public String getCertificate() {
		return certificate;
	}

	public Integer getCertificateType() {
		return certificateType;
	}

	public String getDetailAddress() {
		return detailAddress;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.getId().hashCode();
		result = prime * result + ((carePeople == null) ? 0 : carePeople.hashCode());
		result = prime * result + ((certificate == null) ? 0 : certificate.hashCode());
		result = prime * result + ((certificateType == null) ? 0 : certificateType.hashCode());
		result = prime * result + ((characterType == null) ? 0 : characterType.hashCode());
		result = prime * result + ((detailAddress == null) ? 0 : detailAddress.hashCode());
		result = prime * result + ((education == null) ? 0 : education.hashCode());
		result = prime * result + ((faithType == null) ? 0 : faithType.hashCode());
		result = prime * result + ((hobby == null) ? 0 : hobby.hashCode());
		result = prime * result + ((idols == null) ? 0 : idols.hashCode());
		result = prime * result + ((jobType == null) ? 0 : jobType.hashCode());
		result = prime * result + ((likeBrand == null) ? 0 : likeBrand.hashCode());
		result = prime * result + ((likeContact == null) ? 0 : likeContact.hashCode());
		result = prime * result + ((likeContactTime == null) ? 0 : likeContactTime.hashCode());
		result = prime * result + ((marryStatus == null) ? 0 : marryStatus.hashCode());
		result = prime * result + ((specialDay == null) ? 0 : specialDay.hashCode());
		result = prime * result + ((zodiac == null) ? 0 : zodiac.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		MemberAdditionEntity other = (MemberAdditionEntity) obj;
		if (!this.getId().equals(other.getId()))
			return false;
		if (carePeople == null) {
			if (other.carePeople != null)
				return false;
		} else if (!carePeople.equals(other.carePeople))
			return false;
		if (certificate == null) {
			if (other.certificate != null)
				return false;
		} else if (!certificate.equals(other.certificate))
			return false;
		if (certificateType == null) {
			if (other.certificateType != null)
				return false;
		} else if (!certificateType.equals(other.certificateType))
			return false;
		if (characterType == null) {
			if (other.characterType != null)
				return false;
		} else if (!characterType.equals(other.characterType))
			return false;
		if (detailAddress == null) {
			if (other.detailAddress != null)
				return false;
		} else if (!detailAddress.equals(other.detailAddress))
			return false;
		if (education == null) {
			if (other.education != null)
				return false;
		} else if (!education.equals(other.education))
			return false;
		if (faithType == null) {
			if (other.faithType != null)
				return false;
		} else if (!faithType.equals(other.faithType))
			return false;
		if (hobby == null) {
			if (other.hobby != null)
				return false;
		} else if (!hobby.equals(other.hobby))
			return false;
		if (idols == null) {
			if (other.idols != null)
				return false;
		} else if (!idols.equals(other.idols))
			return false;
		if (jobType == null) {
			if (other.jobType != null)
				return false;
		} else if (!jobType.equals(other.jobType))
			return false;
		if (likeBrand == null) {
			if (other.likeBrand != null)
				return false;
		} else if (!likeBrand.equals(other.likeBrand))
			return false;
		if (likeContact == null) {
			if (other.likeContact != null)
				return false;
		} else if (!likeContact.equals(other.likeContact))
			return false;
		if (likeContactTime == null) {
			if (other.likeContactTime != null)
				return false;
		} else if (!likeContactTime.equals(other.likeContactTime))
			return false;
		if (marryStatus == null) {
			if (other.marryStatus != null)
				return false;
		} else if (!marryStatus.equals(other.marryStatus))
			return false;
		if (specialDay == null) {
			if (other.specialDay != null)
				return false;
		} else if (!specialDay.equals(other.specialDay))
			return false;
		if (zodiac == null) {
			if (other.zodiac != null)
				return false;
		} else if (!zodiac.equals(other.zodiac))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MemberAdditionEntity [carePeople=" + carePeople + ", characterType=" + characterType + ", faithType="
				+ faithType + ", hobby=" + hobby + ", idols=" + idols + ", jobType=" + jobType + ", likeBrand="
				+ likeBrand + ", likeContact=" + likeContact + ", marryStatus=" + marryStatus + ", specialDay="
				+ specialDay + ", zodiac=" + zodiac + ", education=" + education + ", likeContactTime="
				+ likeContactTime + ", certificate=" + certificate + ", certificateType=" + certificateType
				+ ", detailAddress=" + detailAddress + "]";
	}

}
