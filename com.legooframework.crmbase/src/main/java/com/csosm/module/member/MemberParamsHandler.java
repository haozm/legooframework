package com.csosm.module.member;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

import com.csosm.module.member.entity.AssignDTO;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MemberParamsHandler {

	private Map<String, Object> params;

	private Splitter spliter = Splitter.on(",");

	private SimpleDateFormat birthdayFormat = new SimpleDateFormat("yyyy-MM-dd");

	private MemberParamsHandler(Map<String, Object> params) {
		this.params = params;
	}
	
	public static MemberParamsHandler newInstance(Map<String, Object> params) {
		Objects.requireNonNull(params);
		return new MemberParamsHandler(params);
	}
	
	public Optional<String> getOptType() {
		return params.containsKey("type")?Optional.of(MapUtils.getString(params, "type")):Optional.empty();
	}
	
	public String checkAndGetName() {
		Preconditions.checkArgument(params.containsKey("name"), "请求参数缺少name");
		return MapUtils.getString(params, "name");
	}

	public Integer checkAndGetSex() {
		Preconditions.checkArgument(params.containsKey("sex"), "请求参数缺少sex");
		return MapUtils.getInteger(params, "sex");
	}

	public Optional<String> getOptMobilePhone() {
		return params.containsKey("mobilePhone")?Optional.of(getMobilePhone()):Optional.empty();
	}
	
	public String getMobilePhone() {
		return MapUtils.getString(params, "mobilePhone");
	}

	public Optional<Integer> getOptServiceLevel() {
		return params.containsKey("serviceLevel")?Optional.of(getServiceLevel()):Optional.empty();
	}
	
	public Integer getServiceLevel() {
		return MapUtils.getInteger(params, "serviceLevel");
	}

	public Integer checkAndGetEmployeeId() {
		Preconditions.checkArgument(params.containsKey("employeeId"), "请求参数缺少employeeId");
		return getEmployeeId();
	}

	public Optional<Integer> getOptEmployeeId() {
		return params.containsKey("employeeId")?Optional.of(getEmployeeId()):Optional.empty();
	}
	
	public Integer getEmployeeId() {
		return MapUtils.getInteger(params, "employeeId");
	}

	public List<Integer> checkAndGetMemberIds() {
		Preconditions.checkArgument(params.containsKey("memberIds")||params.containsKey("memberId"), "请求参数缺少memberIds或memberId");
		if(params.containsKey("memberIds")) {
			return spliter.splitToList(MapUtils.getString(params, "memberIds")).stream().map(x -> Integer.parseInt(x))
				.collect(Collectors.toList());
		}else {
			return spliter.splitToList(MapUtils.getString(params, "memberId")).stream().map(x -> Integer.parseInt(x))
					.collect(Collectors.toList());
		}
	}
	
	public Optional<Date> getOptCreateCardTime() {
		return params.containsKey("createCardTime")?Optional.of(getCreateCardTime()):Optional.empty();
	}
	
	public List<AssignDTO> checkAndGetAssigns(){
		Preconditions.checkArgument(params.containsKey("assgins"), "请求参数缺少assgins");
		Gson gson = new Gson();
		Type type = new TypeToken<ArrayList<AssignDTO>>(){}.getType();
		return gson.fromJson(MapUtils.getString(params, "assgins"),type);
	}
	
	public Date getCreateCardTime() {
		String createCardTimeStr = MapUtils.getString(params, "createCardTime");
		Date createCardTime = null;
		if (Strings.isNullOrEmpty(createCardTimeStr))
			return createCardTime;
		try {
			createCardTime = birthdayFormat.parse(createCardTimeStr);
		} catch (ParseException e) {
			throw new IllegalArgumentException("生日[birthday]转换时间失败", e);
		}
		return createCardTime;
	}
	
	public Optional<String> getOptMemberCardNum() {
		return params.containsKey("memberCardNum")?Optional.of(getMemberCardNum()):Optional.empty();
	}
	
	public String getMemberCardNum() {
		return MapUtils.getString(params, "memberCardNum");
	}
	
	public Optional<Integer> getOptMemberCardType() {
		return params.containsKey("memberCardType")?Optional.of(getMemberCardType()):Optional.empty();
	} 
	
	public Integer getMemberCardType() {
		return MapUtils.getInteger(params, "memberCardType");
	}
	
	public Optional<Integer> getOptCertificateType() {
		return params.containsKey("certificateType")?Optional.of(getCertificateType()):Optional.empty();
	}
	
	public Integer getCertificateType() {
		return MapUtils.getInteger(params, "certificateType");
	}

	public Optional<Integer> getOptCharacterType() {
		return params.containsKey("characterType")?Optional.of(getCharacterType()):Optional.empty();
	}
	
	public Integer getCharacterType() {
		return MapUtils.getInteger(params, "characterType");
	}

	public Optional<Integer> getOptFaithType() {
		return params.containsKey("faithType")?Optional.of(getFaithType()):Optional.empty();
	}
	
	public Integer getFaithType() {
		return MapUtils.getInteger(params, "faithType");
	}

	public Optional<Integer> getOptLikeContact() {
		return params.containsKey("likeContact")?Optional.of(getLikeContact()):Optional.empty();
	}
	
	public Integer getLikeContact() {
		return MapUtils.getInteger(params, "likeContact");
	}

	public Optional<Integer> getOptEducation() {
		return params.containsKey("education")?Optional.of(getEducation()):Optional.empty();
	}
	
	public Integer getEducation() {
		return MapUtils.getInteger(params, "education");
	}
	
	public Optional<Integer> getOptLikeContactTime() {
		return params.containsKey("likeContactTime")?Optional.of(getLikeContactTime()):Optional.empty();
	}
	
	public Integer getLikeContactTime() {
		return MapUtils.getInteger(params, "likeContactTime");
	}

	public Optional<Integer> getOptZodiac() {
		return params.containsKey("zodiac")?Optional.of(getZodiac()):Optional.empty();
	}
	
	public Integer getZodiac() {
		return MapUtils.getInteger(params, "zodiac");
	}

	public Optional<String> getOptCertificate() {
		return params.containsKey("certificate")?Optional.of(getCertificate()):Optional.empty();
	}
	
	public String getCertificate() {
		return MapUtils.getString(params, "certificate");
	}

	public Optional<String> getOptDetailAddress() {
		return params.containsKey("detailAddress")?Optional.of(getDetailAddress()):Optional.empty();
	}
	
	public String getDetailAddress() {
		return MapUtils.getString(params, "detailAddress");
	}

	public Optional<String> getOptSpecialDay() {
		return params.containsKey("specialDay")?Optional.of(getSpecialDay()):Optional.empty();
	}
	
	public String getSpecialDay() {
		return MapUtils.getString(params, "specialDay");
	}
	
	public Optional<String> getOptLikeBrand() {
		return params.containsKey("likeBrand")?Optional.of(getLikeBrand()):Optional.empty();
	}
	
	public String getLikeBrand() {
		return MapUtils.getString(params, "likeBrand");
	}

	public Optional<String> getOptHobby() {
		return params.containsKey("hobby")?Optional.of(getHobby()):Optional.empty();
	}
	
	public String getHobby() {
		return MapUtils.getString(params, "hobby");
	}

	public Optional<String> getOptJobType() {
		return params.containsKey("jobType")?Optional.of(getJobType()):Optional.empty();
	}
	
	public String getJobType() {
		return MapUtils.getString(params, "jobType");
	}

	public Optional<String> getOptCarePeople() {
		return params.containsKey("carePeople")?Optional.of(getCarePeople()):Optional.empty();
	}
	
	public String getCarePeople() {
		return MapUtils.getString(params, "carePeople");
	}

	public Optional<String> getOptIdols() {
		return params.containsKey("idols")?Optional.of(getIdols()):Optional.empty();
	}
	
	public String getIdols() {
		return MapUtils.getString(params, "idols");
	}

	public Optional<Integer> getOptMarryStatus() {
		return params.containsKey("marryStatus")?Optional.of(getMarryStatus()):Optional.empty();
	}
	
	public Integer getMarryStatus() {
		return MapUtils.getInteger(params, "marryStatus");
	}

	public Optional<String> getOptWeixinId() {
		return params.containsKey("weixinId")?Optional.of(getWeixinId()):Optional.empty();
	}
	
	public String getWeixinId() {
		return MapUtils.getString(params, "weixinId");
	}

	public Optional<String> getOptWeiboNum() {
		return params.containsKey("weiboNum")?Optional.of(getWeiboNum()):Optional.empty();
	}
	
	public String getWeiboNum() {
		return MapUtils.getString(params, "weiboNum");
	}

	public Optional<String> getOptQQNum() {
		return params.containsKey("qqNum")?Optional.of(getQQNum()):Optional.empty();
	}
	
	public String getQQNum() {
		return MapUtils.getString(params, "qqNum");
	}

	public Optional<String> getOptTelephone() {
		return params.containsKey("telephone")?Optional.of(getTelephone()):Optional.empty();
	}
	
	public String getTelephone() {
		return MapUtils.getString(params, "telephone");
	}

	public Optional<String> getOptMobilephone() {
		return params.containsKey("mobilePhone")?Optional.of(getMobilephone()):Optional.empty();
	}
	
	public String getMobilephone() {
		return MapUtils.getString(params, "mobilePhone");
	}

	public Optional<String> getOptEmail() {
		return params.containsKey("email")?Optional.of(getEmail()):Optional.empty();
	}
	
	public String getEmail() {
		return MapUtils.getString(params, "email");
	}

	public Optional<Integer> getOptMemberType() {
		return params.containsKey("memberType")?Optional.of(getMemberType()):Optional.empty();
	}
	
	public Integer getMemberType() {
		return MapUtils.getInteger(params, "memberType");
	}

	public Optional<Integer> getOptCalendarType() {
		return params.containsKey("calendarType")?Optional.of(getCalendarType()):Optional.empty();
	}
	
	public Integer getCalendarType() {
		return MapUtils.getInteger(params, "calendarType");
	}
	
	public Optional<Date> getOptLunarBirthday() {
		return params.containsKey("lunarBirthday")?Optional.of(getLunarBirthday()):Optional.empty();
	}

	public Date getLunarBirthday() {
		String lunarBirthdayStr = MapUtils.getString(params, "lunarBirthday");
		Date lunarBirthday = null;
		if (Strings.isNullOrEmpty(lunarBirthdayStr))
			return lunarBirthday;
		try {
			lunarBirthday = birthdayFormat.parse(lunarBirthdayStr);
		} catch (ParseException e) {
			throw new IllegalArgumentException("生日[lunarBirthday]转换时间失败", e);
		}
		return lunarBirthday;
	}

	public Optional<Date> getOptBirthday() {
		return params.containsKey("birthday")?Optional.of(getBirthday()):Optional.empty();
	}
	
	public Date getBirthday() {
		String birthdayStr = MapUtils.getString(params, "birthday");
		Date birthday = null;
		if (Strings.isNullOrEmpty(birthdayStr))
			return birthday;
		try {
			birthday = birthdayFormat.parse(birthdayStr);
		} catch (ParseException e) {
			throw new IllegalArgumentException("生日[birthday]转换时间失败", e);
		}
		return birthday;
	}
	
	public Optional<Integer> getOptMemberId(){
		return params.containsKey("memberId")?Optional.of(MapUtils.getInteger(params, "memberId")):Optional.empty();
	}

	public Integer checkAndGetMemberId() {
		Preconditions.checkArgument(params.containsKey("memberId"), "请求参数缺少memberId");
		return MapUtils.getInteger(params, "memberId");
	}
	
	public Optional<String> getOptIconUrl() {
		return params.containsKey("iconUrl")?Optional.of(getIconUrl()):Optional.empty();
	}
	
	public String getIconUrl() {
		return MapUtils.getString(params, "iconUrl");
	}

	public Optional<String> getOptNamePinyin() {
		return params.containsKey("namePinyin")?Optional.of(getNamePinyin()):Optional.empty();
	}
	
	public String getNamePinyin() {
		return MapUtils.getString(params, "namePinyin");
	}
	
	public Optional<String> getOptJacketSize() {
		return params.containsKey("jacketSize")?Optional.of(getJacketSize()):Optional.empty();
	}
	
	public String getJacketSize() {
		return MapUtils.getString(params, "jacketSize");
	}
	
	public Optional<String> getOptBottomsSize() {
		return params.containsKey("bottomsSize")?Optional.of(getBottomsSize()):Optional.empty();
	}
	
	public String getBottomsSize() {
		return MapUtils.getString(params, "bottomsSize");
	}
	
	public Optional<String> getOptBraSize() {
		return params.containsKey("braSize")?Optional.of(getBraSize()):Optional.empty();
	}
	
	public String getBraSize() {
		return MapUtils.getString(params, "braSize");
	}
	
	public Optional<String> getOptBriefsSize() {
		return params.containsKey("briefsSize")?Optional.of(getBriefsSize()):Optional.empty();
	}
	
	public String getBriefsSize() {
		return MapUtils.getString(params, "briefsSize");
	}
	
	public Optional<String> getOptShoeSize() {
		return params.containsKey("shoeSize")?Optional.of(getShoeSize()):Optional.empty();
	}
	
	public String getShoeSize() {
		return MapUtils.getString(params, "shoeSize");
	}
	
	public Optional<BigDecimal> getOptChest() {
		return params.containsKey("chest")?Optional.of(getChest()):Optional.empty();
	}
	
	public BigDecimal getChest() {
		return MapUtils.getString(params, "chest") == null ? null
				: new BigDecimal(MapUtils.getString(params, "chest"));
	}
	
	public Optional<BigDecimal> getOptClothingLong() {
		return params.containsKey("clothingLong")?Optional.of(getClothingLong()):Optional.empty();
	}
	
	public BigDecimal getClothingLong() {
		return MapUtils.getString(params, "clothingLong") == null ? null
				: new BigDecimal(MapUtils.getString(params, "clothingLong"));
	}
	
	public Optional<BigDecimal> getOptSleeveLength() {
		return params.containsKey("sleeveLength")?Optional.of(getSleeveLength()):Optional.empty();
	}
	
	public BigDecimal getSleeveLength() {
		return MapUtils.getString(params, "sleeveLength") == null ? null
				: new BigDecimal(MapUtils.getString(params, "sleeveLength"));
	}
	
	public Optional<BigDecimal> getOptShoulder() {
		return params.containsKey("shoulder")?Optional.of(getShoulder()):Optional.empty();
	}
	
	public BigDecimal getShoulder() {
		return MapUtils.getString(params, "shoulder") == null ? null
				: new BigDecimal(MapUtils.getString(params, "shoulder"));
	}
	
	public Optional<BigDecimal> getOptWaistline() {
		return params.containsKey("waistline")?Optional.of(getWaistline()):Optional.empty();
	}
	
	public BigDecimal getWaistline() {
		return MapUtils.getString(params, "waistline") == null ? null
				: new BigDecimal(MapUtils.getString(params, "waistline"));
	}
	
	public Optional<BigDecimal> getOptHipline() {
		return params.containsKey("hipline")?Optional.of(getHipline()):Optional.empty();
	}
	
	public BigDecimal getHipline() {
		return MapUtils.getString(params, "hipline") == null ? null
				: new BigDecimal(MapUtils.getString(params, "hipline"));
	}
	
	public Optional<BigDecimal> getOptThighCircumference() {
		return params.containsKey("thighCircumference")?Optional.of(getThighCircumference()):Optional.empty();
	}
	
	public BigDecimal getThighCircumference() {
		return MapUtils.getString(params, "thighCircumference") == null ? null
				: new BigDecimal(MapUtils.getString(params, "thighCircumference"));
	}
	
	public Optional<BigDecimal> getOptKneeCircumference() {
		return params.containsKey("kneeCircumference")?Optional.of(getKneeCircumference()):Optional.empty();
	}
	
	public BigDecimal getKneeCircumference() {
		return MapUtils.getString(params, "kneeCircumference") == null ? null
				: new BigDecimal(MapUtils.getString(params, "kneeCircumference"));
	}
	
	public Optional<BigDecimal> getOptTrouserLeg() {
		return params.containsKey("trouserLeg")?Optional.of(getTrouserLeg()):Optional.empty();
	}
	
	public BigDecimal getTrouserLeg() {
		return MapUtils.getString(params, "trouserLeg") == null ? null
				: new BigDecimal(MapUtils.getString(params, "trouserLeg"));
	}
	
	public Optional<BigDecimal> getOptBeforeFork() {
		return params.containsKey("beforeFork")?Optional.of(getBeforeFork()):Optional.empty();
	}
	
	public BigDecimal getBeforeFork() {
		return MapUtils.getString(params, "beforeFork") == null ? null
				: new BigDecimal(MapUtils.getString(params, "beforeFork"));
	}
	
	public Optional<BigDecimal> getOptAfterFork() {
		return params.containsKey("afterFork")?Optional.of(getAfterFork()):Optional.empty();
	}
	
	public BigDecimal getAfterFork() {
		return MapUtils.getString(params, "afterFork") == null ? null
				: new BigDecimal(MapUtils.getString(params, "afterFork"));
	}
	
	public Optional<BigDecimal> getOptOutseam() {
		return params.containsKey("outseam")?Optional.of(getOutseam()):Optional.empty();
	}
	
	public BigDecimal getOutseam() {
		return MapUtils.getString(params, "outseam") == null ? null
				: new BigDecimal(MapUtils.getString(params, "outseam"));
	}
	
	public Optional<BigDecimal> getOptOnChest() {
		return params.containsKey("onChest")?Optional.of(getOnChest()):Optional.empty();
	}
	
	public BigDecimal getOnChest() {
		return MapUtils.getString(params, "onChest") == null ? null
				: new BigDecimal(MapUtils.getString(params, "onChest"));
	}
	
	public Optional<BigDecimal> getOptUnderChest() {
		return params.containsKey("underChest")?Optional.of(getUnderChest()):Optional.empty();
	}
	
	public BigDecimal getUnderChest() {
		return MapUtils.getString(params, "underChest") == null ? null
				: new BigDecimal(MapUtils.getString(params, "underChest"));
	}
	
	public Optional<BigDecimal> getOptFootLength() {
		return params.containsKey("footLength")?Optional.of(getFootLength()):Optional.empty();
	}
	
	public BigDecimal getFootLength() {
		return MapUtils.getString(params, "footLength") == null ? null
				: new BigDecimal(MapUtils.getString(params, "footLength"));
	}
}
