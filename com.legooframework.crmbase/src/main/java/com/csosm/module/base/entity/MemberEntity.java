package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.entity.Replaceable;
import com.google.common.base.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class MemberEntity extends BaseEntity<Integer> implements Replaceable {
    // 姓名
    private String name;
    // 会员类型
    private Integer memberType;
    // 性别
    private Integer sex;
    // 服务等级：1 - 粉丝会员，2 - 积分会员，3 - 储值会员
    private Integer serviceLevel;
    // 电话号码
    private String phone;
    // 卡类型
    private Integer memberCardType;
    // 卡号码
    private String memberCardNum;
    // 开卡时间
    private Date createCardTime;
    // QQ号码
    private String qqNum;
    // 微信ID
    private String weixinId;
    // 微信信息
    private WeixinAct weixinInfo;
    // 微博
    private String weiboNum;
    // 生日
    private Birthday birthday;
    // 邮箱
    private String email;
    // 婚姻状况
    private Integer marryStatus;
    // 家庭住址
    private String detailAddress;
    // 崇拜的人
    private String idols;
    // 在乎的人
    private String carePeople;
    // 星座
    private Integer zodiac;
    // 性格特征： 1 - 视觉型， 2 - 听觉型， 3 - 感觉型，4 - 混合型
    private Integer characterType;
    // 工作行业
    private String jobType;
    // 信仰
    private Integer faithType;
    // 最佳联系方式
    private Integer likeContact;

    private String telephone;
    // 1 - 身份证， 2 - 员工证件， 3 - 其它
    private Integer certificateType;
    // 证件号
    private String certificate;
    // 兼容旧系统数据
    private String oldMemberCode;
    // 有效标准 2：无效，其他有效
    private Integer effectiveFlag;
    // 公司id
    private final Integer companyId;

    private Integer status;

    private Set<Integer> storeIds;
    // 是否有微信账号
    private Integer hasWeixinAccount;
    // 门店RFM
    private String storeRFM;
    // 公司RFM
    private String comRFM;
    // 总积分
    private Integer totalScore;
    private Integer assignState;
    // 开卡门店
    private final CardStore cardStore;

    private String iconUrl;

    private Set<Integer> shoppingGuideIds;

    public MemberEntity(String name, Integer memberType, Integer sex, Integer serviceLevel, String phone,
                        Integer memberCardType, String memberCardNum, Date createCardTime, Integer createCardStoreId,
                        String createCardStoreName, String weixinId, String qqNum, String weiboNum, String email,
                        Date gregorianBirthday, Date lunarBirthday, Integer calendarType, String iconUrl, Integer marryStatus,
                        String detailAddress, String idols, String carePeople, Integer zodiac, Integer characterType,
                        String jobType, Integer faithType, Integer likeContact, StoreEntity store) {
        super(0);
        this.name = name;
        this.memberType = memberType;
        this.sex = sex;
        this.serviceLevel = serviceLevel;
        this.phone = phone;
        this.memberCardType = memberCardType;
        this.memberCardNum = memberCardNum;
        this.createCardTime = createCardTime;
        CardStore cardStore = new CardStore(createCardStoreId == null ? null : String.valueOf(createCardStoreId), createCardStoreName);
        this.cardStore = cardStore;
        this.weixinId = weixinId;
        this.qqNum = qqNum;
        this.weiboNum = weiboNum;
        this.email = email;
        Birthday birthday = new Birthday(gregorianBirthday, lunarBirthday, calendarType);
        this.birthday = birthday;
        this.iconUrl = iconUrl;
        this.marryStatus = marryStatus;
        this.detailAddress = detailAddress;
        this.idols = idols;
        this.carePeople = carePeople;
        this.zodiac = zodiac;
        this.characterType = characterType;
        this.jobType = jobType;
        this.faithType = faithType;
        this.likeContact = likeContact;
        Preconditions.checkNotNull(store);
        this.storeIds = Sets.newHashSet(store.getId());
        Preconditions.checkArgument(store.getCompanyId().isPresent(), "门店公司不能为空");
        this.companyId = store.getCompanyId().get();
    }

    public MemberEntity(Integer id, String name, Integer sex, String phone, Birthday birthday, String telephone,
                        Integer certificateType, String certificate, String oldMemberCode, Integer effectiveFlag,
                        Integer serviceLevel, Integer memberType, Integer companyId, Integer status, Integer hasWeixinAccount,
                        Integer storeIds, String weixinId, String storeRFM, String comRFM, Integer totalScore, WeixinAct weixinInfo,
                        String memberCardNum, CardStore cardStore, Date createCardTime, Integer memberCardType, String qqNum,
                        String weiboNum, String email, Integer marryStatus, String detailAddress, String idols, String carePeople,
                        Integer zodiac, Integer characterType, String jobType, Integer faithType, Integer likeContact,
                        String iconUrl, Set<Integer> shoppingGuideIds) {
        super(id);
        this.name = name;
        this.sex = sex;
        this.phone = phone;
        this.birthday = birthday;
        this.telephone = telephone;
        this.certificateType = certificateType;
        this.certificate = certificate;
        this.oldMemberCode = oldMemberCode;
        this.effectiveFlag = effectiveFlag;
        this.serviceLevel = serviceLevel;
        this.memberType = memberType;
        this.companyId = companyId;
        this.status = status;
        this.weixinId = Strings.emptyToNull(weixinId);
        this.hasWeixinAccount = hasWeixinAccount;
        this.storeIds = Sets.newHashSet(storeIds);
        this.storeRFM = storeRFM;
        this.comRFM = comRFM;
        this.totalScore = totalScore;
        this.weixinInfo = weixinInfo;
        this.memberCardNum = memberCardNum;
        this.cardStore = cardStore;
        this.createCardTime = createCardTime;
        this.memberCardType = memberCardType;
        this.qqNum = qqNum;
        this.weiboNum = weiboNum;
        this.email = email;
        this.marryStatus = marryStatus;
        this.detailAddress = detailAddress;
        this.idols = idols;
        this.carePeople = carePeople;
        this.zodiac = zodiac;
        this.characterType = characterType;
        this.jobType = jobType;
        this.faithType = faithType;
        this.likeContact = likeContact;
        this.iconUrl = iconUrl;
        if (!CollectionUtils.isEmpty(shoppingGuideIds)) {
            this.shoppingGuideIds = Sets.newHashSet(shoppingGuideIds);
            this.assignState = 1;
        } else {
            this.assignState = 2;
        }

    }

    public MemberEntity modify(String name, Integer memberType, Integer sex, Integer serviceLevel, String phone,
                               Integer memberCardType, String memberCardNum, Date createCardTime, String qqNum, String weixinId,
                               String weiboNum, String email, Integer marryStatus, String detailAddress, String idols, String carePeople,
                               Integer zodiac, Integer characterType, String jobType, Integer faithType, Integer likeContact,
                               Date gregorianBirthday, Date lunarBirthday, String iconUrl, Integer calendarType) {
        MemberEntity clone = null;
        try {
            clone = (MemberEntity) this.clone();
            clone.name = name;
            clone.memberType = memberType;
            clone.sex = sex;
            clone.serviceLevel = serviceLevel;
            clone.phone = phone;
            clone.memberCardType = memberCardType;
            clone.memberCardNum = memberCardNum;
            clone.createCardTime = createCardTime;
            clone.qqNum = qqNum;
            clone.weixinId = weixinId;
            clone.weiboNum = weiboNum;
            clone.email = email;
            clone.marryStatus = marryStatus;
            clone.detailAddress = detailAddress;
            clone.idols = idols;
            clone.carePeople = carePeople;
            clone.zodiac = zodiac;
            clone.characterType = characterType;
            clone.jobType = jobType;
            clone.faithType = faithType;
            clone.likeContact = likeContact;
            Birthday birthday = this.birthday.modify(gregorianBirthday, lunarBirthday, calendarType);
            clone.birthday = birthday;
            clone.iconUrl = iconUrl;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("修改属性发生异常");
        }
        return clone;
    }

    Optional<MemberEntity> makedUneffective() {
        if (!isEffective()) return Optional.absent();
        try {
            MemberEntity me = (MemberEntity) this.clone();
            me.effectiveFlag = 2;
            return Optional.of(me);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    Optional<MemberEntity> removeAllShoppingGuide() {
        try {
            if (assignState == 2) return Optional.absent();
            if (CollectionUtils.isEmpty(this.shoppingGuideIds)) return Optional.absent();
            MemberEntity clone = (MemberEntity) this.clone();
            clone.shoppingGuideIds = null;
            clone.assignState = 2;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Optional<MemberEntity> removeShoppingGuide(EmployeeEntity employee) {
        try {
            if (assignState == 2) return Optional.absent();
            if (!CollectionUtils.isEmpty(this.shoppingGuideIds) && this.shoppingGuideIds.contains(employee.getId())) {
                MemberEntity clone = (MemberEntity) this.clone();
                clone.shoppingGuideIds = Sets.newHashSet(this.shoppingGuideIds);
                clone.shoppingGuideIds.remove(employee.getId());
                if (CollectionUtils.isEmpty(clone.shoppingGuideIds)) {
                    clone.shoppingGuideIds = null;
                    clone.assignState = 2;
                    return Optional.of(clone);
                }
                return Optional.absent();
            }
            return Optional.absent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Optional<MemberEntity> addShoppingGuide(EmployeeEntity employee) {
        try {
            if (assignState == 1) return Optional.absent();
            MemberEntity clone = (MemberEntity) this.clone();
            clone.assignState = 1;
            return Optional.of(clone);
//            if (this.shoppingGuideIds == null) {
//                MemberEntity _con = (MemberEntity) this.clone();
//                _con.shoppingGuideIds = Sets.newHashSet();
//                clone.shoppingGuideIds.add(employee.getId());
//            } else {
//                if (this.shoppingGuideIds.contains(employee.getId())) return Optional.absent();
//                MemberEntity clone = (MemberEntity) this.clone();
//                clone.shoppingGuideIds = Sets.newHashSet();
//                clone.shoppingGuideIds.add(employee.getId());
//                return Optional.of(clone);
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Integer getAssignState() {
        return assignState;
    }

    static class CardStore {

        private final String id;

        private final String name;

        CardStore(String id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CardStore cardStore = (CardStore) o;
            return Objects.equal(id, cardStore.id) &&
                    Objects.equal(name, cardStore.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, name);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("name", name)
                    .toString();
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> paramMap = super.toMap();
        paramMap.put("memberId", this.getId());
        paramMap.put("name", this.name);
        paramMap.put("sex", this.sex);
        paramMap.put("calendarType", this.getBirthday() == null ? null : this.getBirthday().getCalendarType());
        paramMap.put("serviceLevel", this.serviceLevel);
        if (this.getBirthday() != null) {
            String gregorianDate = this.getBirthday().getGregorianDate() != null
                    ? DateFormatUtils.format(this.getBirthday().getGregorianDate(), "yyyy-MM-dd")
                    : null;
            paramMap.put("gregorianBirthday", gregorianDate);
            String lunarDate = this.getBirthday().getLunarDate() != null
                    ? DateFormatUtils.format(this.getBirthday().getLunarDate(), "yyyy-MM-dd")
                    : null;
            paramMap.put("lunarBirthday", lunarDate);
        } else {
            paramMap.put("gregorianBirthday", null);
            paramMap.put("lunarBirthday", null);
        }

        paramMap.put("phone", this.getPhone());
        paramMap.put("memberType", this.memberType);
        paramMap.put("memberCardType", this.memberCardType);
        paramMap.put("memberCardNum", this.memberCardNum);
        String createCardTime = this.createCardTime != null ? DateFormatUtils.format(this.createCardTime, "yyyy-MM-dd")
                : null;
        paramMap.put("createCardTime", createCardTime);
        paramMap.put("qqNum", this.qqNum);
        paramMap.put("weixinId", this.weixinId);
        paramMap.put("weiboNum", this.weiboNum);
        paramMap.put("email", this.email);
        paramMap.put("marryStatus", this.marryStatus);
        paramMap.put("detailAddress", this.detailAddress);
        paramMap.put("idols", this.idols);
        paramMap.put("carePeople", this.carePeople);
        paramMap.put("zodiac", this.zodiac);
        paramMap.put("characterType", this.characterType);
        paramMap.put("jobType", this.jobType);
        paramMap.put("faithType", this.faithType);
        paramMap.put("likeContact", this.likeContact);
        paramMap.put("iconUrl", this.iconUrl);
        if (this.cardStore != null) {
            paramMap.put("createStoreId", Strings.isNullOrEmpty(this.cardStore.getId()) ? null : this.cardStore.getId());
        } else {
            paramMap.put("createStoreId", null);
        }
        if (CollectionUtils.isEmpty(this.shoppingGuideIds)) {
            paramMap.put("assignState", 2);
        } else {
            paramMap.put("assignState", 1);
        }
        paramMap.put("companyId", this.companyId);
        paramMap.put("storeId", this.storeIds.iterator().next());
        return paramMap;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberId", getId());
        params.put("sex", this.sex);
        params.put("name", this.name);
        params.put("phoneNo", this.phone);
        params.put("memberType", this.memberType);
        params.put("assignState", this.assignState);
        params.put("calendarType", birthday == null ? "" : birthday.getCalendarType());
        if (birthday.getCalendarType() == 1) {
            params.put("birthday", birthday == null || birthday.getGregorianDate() == null ? ""
                    : DateFormatUtils.format(birthday.getGregorianDate(), "yyyy-MM-dd"));
        } else if (birthday.getCalendarType() == 2) {
            params.put("birthday", birthday == null || birthday.getLunarDate() == null ? ""
                    : DateFormatUtils.format(birthday.getLunarDate(), "yyyy-MM-dd"));
        } else {
            params.put("birthday", null);
        }
        params.put("createCardStoreId",
                this.cardStore == null || this.cardStore.getId() == null ? "" : this.cardStore.getId());
        params.put("createCardStoreName",
                this.cardStore == null || this.cardStore.getName() == null ? "" : this.cardStore.getName());
        params.put("createCardTime",
                this.getCreateCardTime() == null ? "" : DateFormatUtils.format(this.getCreateCardTime(), "yyyy-MM-dd"));
        params.put("serviceLevelName", "");
        params.put("serviceLevel", this.serviceLevel);
        if (serviceLevel != null)
            switch (serviceLevel) {
                case 1:
                    params.put("serviceLevelName", "粉丝会员");
                    break;
                case 2:
                    params.put("serviceLevelName", "积分会员");
                    break;
                case 3:
                    params.put("serviceLevelName", "储值会员");
                    break;
                default:
                    params.put("serviceLevelName", "");
                    break;
            }
        params.put("memberCardNum", memberCardNum);
        params.put("certificateName", "");
        // 服务等级：1 - 粉丝会员，2 - 积分会员，3 - 储值会员
        if (certificateType != null) {
            switch (this.certificateType) {
                case 1:
                    params.put("certificateName", "身份证");
                    break;
                case 2:
                    params.put("certificateName", "员工证件");
                    break;
                case 3:
                    params.put("certificateName", "其他证件");
                    break;
                default:
                    break;
            }
        }
        params.put("certificateType", this.certificateType);
        params.put("certificate", this.certificate);
        params.put("srfm", storeRFM);
        params.put("crfm", comRFM);
        params.put("serviceLevel", serviceLevel);
        params.put("totalScore", totalScore);
        params.put("weixinId", weixinId);
        if (weixinInfo != null) {
            params.put("nickName", Strings.isNullOrEmpty(weixinInfo.getNickName()) ? null : weixinInfo.getNickName());
            params.put("iconUrl", Strings.isNullOrEmpty(this.iconUrl) ? this.iconUrl : (Strings.isNullOrEmpty(weixinInfo.getIconUrl()) ? null : weixinInfo.getIconUrl()));
            params.put("remark", Strings.isNullOrEmpty(weixinInfo.getRemark()) ? null : weixinInfo.getRemark());
        } else {
            params.put("nickName", null);
            params.put("iconUrl", null);
            params.put("remark", null);
        }
        params.put("memberCardType", this.memberCardType);
        params.put("qqNum", this.qqNum);
        params.put("weiboNum", this.weiboNum);
        params.put("email", this.email);
        params.put("marryStatus", this.marryStatus);
        params.put("detailAddress", this.detailAddress);
        params.put("idols", this.idols);
        params.put("carePeople", this.carePeople);
        params.put("zodiac", this.zodiac);
        params.put("characterType", this.characterType);
        params.put("jobType", this.jobType);
        params.put("faithType", this.faithType);
        params.put("likeContact", this.likeContact);
        params.put("storeId", this.getStoreId().isPresent() ? this.getStoreId().get() : "");
        return params;
    }

    public boolean isValid() {
        return status != null && status == 1;
    }

    public boolean hasWeixinAccount() {
        return !Strings.isNullOrEmpty(this.weixinId);
    }

    public boolean isFansMember() {
        return serviceLevel != null && serviceLevel == 1;
    }

    public boolean isJiFenMember() {
        return serviceLevel != null && serviceLevel == 2;
    }

    public boolean isChuZhiMember() {
        return serviceLevel != null && serviceLevel == 3;
    }

    public String getStoreRFM() {
        return storeRFM;
    }

    public String getComRFM() {
        return comRFM;
    }

    public boolean isEffective() {
        if (effectiveFlag == null) return true;
        return effectiveFlag != 2;
    }

    public String getName() {
        return name;
    }

    public Integer getSex() {
        return sex;
    }

    public String getPhone() {
        return phone;
    }

    public Optional<Integer> getStoreId() {
        if (CollectionUtils.isEmpty(this.storeIds))
            return Optional.absent();
        Preconditions.checkArgument(this.storeIds.size() == 1, "当前会员属于多家门店...");
        return Optional.of(this.storeIds.iterator().next());
    }

    public Birthday getBirthday() {
        return birthday;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getOldMemberCode() {
        return oldMemberCode;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Set<Integer> getStoreIds() {
        return storeIds;
    }

    public CardStore getCardStore() {
        return cardStore;
    }

    public boolean isOwnStore(StoreEntity store) {
        if (CollectionUtils.isEmpty(storeIds))
            return false;
        return storeIds.contains(store.getId());
    }

    public String getMemberCardNum() {
        return memberCardNum;
    }

    public Optional<String> getWeixinId() {
        return Optional.fromNullable(weixinId);
    }

    public boolean hasPhone() {
        return StringUtils.isNotEmpty(this.phone);
    }

    @Override
    public Map<String, String> toSmsMap(StoreEntity store) {
        Map<String, String> sms = Maps.newHashMap();
        sms.put("{会员姓名}", getName() == null ? "" : getName());
        sms.put("{会员编号}", this.getId() == null ? "" : this.getId().toString());

        String sex = "";
        if (this.sex != null) {
            if (this.sex == 1) {
                sex = "男";
            } else if (this.sex == 2) {
                sex = "女";
            } else {
                sex = "其他";
            }
        } else {
            sex = "其他";
        }

        sms.put("{会员性别}", sex);

        String birthday = "";
        if (this.getBirthday() != null && this.getBirthday().getCalendarType() == 1) {
            birthday = this.getBirthday().getGregorianDate() != null
                    ? String.format("公历[%s]", DateFormatUtils.format(this.getBirthday().getGregorianDate(), "yyyy-MM-dd"))
                    : "";
        }
        if (this.getBirthday() != null && this.getBirthday().getCalendarType() == 2) {
            birthday = this.getBirthday().getLunarDate() != null
                    ? String.format("农历[%s]", DateFormatUtils.format(this.getBirthday().getLunarDate(), "yyyy-MM-dd"))
                    : "";
        }
        sms.put("{会员生日}", birthday);

        String serviceLevel = "";
        if (this.serviceLevel != null) {
            if (this.serviceLevel == 1) {
                serviceLevel = "粉丝服务";
            } else if (this.serviceLevel == 2) {
                serviceLevel = "积分服务";
            } else if (this.serviceLevel == 3) {
                serviceLevel = "储值服务";
            }
        } else {
            serviceLevel = "未知等级";
        }

        sms.put("{会员服务等级}", serviceLevel);

        sms.put("{会员电话号码}", this.getPhone() == null ? "" : this.getPhone());

        String memberType = "";
        if (this.memberType != null) {
            if (this.memberType == 1) memberType = "粉丝会员";
            if (this.memberType == 2) memberType = "普通会员";
        } else {
            memberType = "未知类型";
        }

        sms.put("{会员类型}", memberType);

        sms.put("{会员卡号}", this.memberCardNum == null ? "" : this.memberCardNum);
        sms.put("{开卡时间}", this.createCardTime != null ? DateFormatUtils.format(this.createCardTime, "yyyy-MM-dd")
                : "");
        sms.put("{会员QQ号}", this.qqNum == null ? "" : this.qqNum);
        sms.put("{会员微信账号}", this.weixinId == null ? "" : this.weixinId);
        sms.put("{会员微博账号}", this.weiboNum == null ? "" : this.weiboNum);
        sms.put("{会员邮箱}", this.email == null ? "" : this.email);

        String marryStatus = "";
        if (this.marryStatus != null) {
            if (this.marryStatus == 0) marryStatus = "未婚";
            if (this.marryStatus == 1) marryStatus = "已婚";
            if (this.marryStatus == 2) marryStatus = "离异";
            if (this.marryStatus == 3) marryStatus = "再婚";
        } else {
            marryStatus = "未知";
        }

        sms.put("{会员婚姻状况}", marryStatus);
        sms.put("{会员详细地址}", this.detailAddress == null ? "" : this.detailAddress);
        return sms;
    }

    public Integer getMemberType() {
        return memberType;
    }

    public Integer getServiceLevel() {
        return serviceLevel;
    }

    public Integer getMemberCardType() {
        return memberCardType;
    }

    public String getQqNum() {
        return qqNum;
    }

    public WeixinAct getWeixinInfo() {
        return weixinInfo;
    }

    public String getWeiboNum() {
        return weiboNum;
    }

    public String getEmail() {
        return email;
    }

    public Integer getMarryStatus() {
        return marryStatus;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public String getIdols() {
        return idols;
    }

    public String getCarePeople() {
        return carePeople;
    }

    public Integer getZodiac() {
        return zodiac;
    }

    public Integer getCharacterType() {
        return characterType;
    }

    public String getJobType() {
        return jobType;
    }

    public Integer getFaithType() {
        return faithType;
    }

    public Integer getLikeContact() {
        return likeContact;
    }

    public Integer getCertificateType() {
        return certificateType;
    }

    public Integer getEffectiveFlag() {
        return effectiveFlag;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getHasWeixinAccount() {
        return hasWeixinAccount;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public boolean equalsModifyInfo(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        MemberEntity other = (MemberEntity) obj;
        if (birthday == null) {
            if (other.birthday != null)
                return false;
        } else if (!birthday.equals(other.birthday))
            return false;
        if (carePeople == null) {
            if (other.carePeople != null)
                return false;
        } else if (!carePeople.equals(other.carePeople))
            return false;
        if (characterType == null) {
            if (other.characterType != null)
                return false;
        } else if (!characterType.equals(other.characterType))
            return false;
        if (createCardTime == null) {
            if (other.createCardTime != null)
                return false;
        } else if (!createCardTime.equals(other.createCardTime))
            return false;
        if (detailAddress == null) {
            if (other.detailAddress != null)
                return false;
        } else if (!detailAddress.equals(other.detailAddress))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (faithType == null) {
            if (other.faithType != null)
                return false;
        } else if (!faithType.equals(other.faithType))
            return false;
        if (iconUrl == null) {
            if (other.iconUrl != null)
                return false;
        } else if (!iconUrl.equals(other.iconUrl))
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
        if (likeContact == null) {
            if (other.likeContact != null)
                return false;
        } else if (!likeContact.equals(other.likeContact))
            return false;
        if (marryStatus == null) {
            if (other.marryStatus != null)
                return false;
        } else if (!marryStatus.equals(other.marryStatus))
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
        if (memberType == null) {
            if (other.memberType != null)
                return false;
        } else if (!memberType.equals(other.memberType))
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
        if (qqNum == null) {
            if (other.qqNum != null)
                return false;
        } else if (!qqNum.equals(other.qqNum))
            return false;
        if (serviceLevel == null) {
            if (other.serviceLevel != null)
                return false;
        } else if (!serviceLevel.equals(other.serviceLevel))
            return false;
        if (sex == null) {
            if (other.sex != null)
                return false;
        } else if (!sex.equals(other.sex))
            return false;
        if (weiboNum == null) {
            if (other.weiboNum != null)
                return false;
        } else if (!weiboNum.equals(other.weiboNum))
            return false;
        if (weixinId == null) {
            if (other.weixinId != null)
                return false;
        } else if (!weixinId.equals(other.weixinId))
            return false;
        if (zodiac == null) {
            if (other.zodiac != null)
                return false;
        } else if (!zodiac.equals(other.zodiac))
            return false;
        return true;
    }

    public Date getCreateCardTime() {
        return createCardTime;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        MemberEntity that = (MemberEntity) o;
        return Objects.equal(name, that.name) && Objects.equal(memberType, that.memberType)
                && Objects.equal(sex, that.sex) && Objects.equal(serviceLevel, that.serviceLevel)
                && Objects.equal(phone, that.phone) && Objects.equal(memberCardType, that.memberCardType)
                && Objects.equal(memberCardNum, that.memberCardNum)
                && Objects.equal(createCardTime, that.createCardTime) && Objects.equal(qqNum, that.qqNum)
                && Objects.equal(weixinId, that.weixinId) && Objects.equal(weixinInfo, that.weixinInfo)
                && Objects.equal(weiboNum, that.weiboNum) && Objects.equal(birthday, that.birthday)
                && Objects.equal(email, that.email) && Objects.equal(marryStatus, that.marryStatus)
                && Objects.equal(detailAddress, that.detailAddress) && Objects.equal(idols, that.idols)
                && Objects.equal(carePeople, that.carePeople) && Objects.equal(zodiac, that.zodiac)
                && Objects.equal(characterType, that.characterType) && Objects.equal(jobType, that.jobType)
                && Objects.equal(faithType, that.faithType) && Objects.equal(likeContact, that.likeContact)
                && Objects.equal(telephone, that.telephone) && Objects.equal(certificateType, that.certificateType)
                && Objects.equal(certificate, that.certificate) && Objects.equal(oldMemberCode, that.oldMemberCode)
                && Objects.equal(effectiveFlag, that.effectiveFlag) && Objects.equal(companyId, that.companyId)
                && Objects.equal(status, that.status) && Objects.equal(storeIds, that.storeIds)
                && Objects.equal(hasWeixinAccount, that.hasWeixinAccount) && Objects.equal(storeRFM, that.storeRFM)
                && Objects.equal(comRFM, that.comRFM) && Objects.equal(totalScore, that.totalScore)
                && Objects.equal(cardStore, that.cardStore) && Objects.equal(iconUrl, that.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, memberType, sex, serviceLevel, phone, memberCardType,
                memberCardNum, createCardTime, qqNum, weixinId, weixinInfo, weiboNum, birthday, email, marryStatus,
                detailAddress, idols, carePeople, zodiac, characterType, jobType, faithType, likeContact, telephone,
                certificateType, certificate, oldMemberCode, effectiveFlag, companyId, status, storeIds,
                hasWeixinAccount, storeRFM, comRFM, totalScore, cardStore, iconUrl);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("memberType", memberType).add("sex", sex)
                .add("serviceLevel", serviceLevel).add("phone", phone).add("memberCardType", memberCardType)
                .add("memberCardNum", memberCardNum).add("createCardTime", createCardTime).add("qqNum", qqNum)
                .add("weixinId", weixinId).add("weixinInfo", weixinInfo).add("weiboNum", weiboNum)
                .add("birthday", birthday).add("email", email).add("marryStatus", marryStatus)
                .add("detailAddress", detailAddress).add("idols", idols).add("carePeople", carePeople)
                .add("zodiac", zodiac).add("characterType", characterType).add("jobType", jobType)
                .add("faithType", faithType).add("likeContact", likeContact).add("telephone", telephone)
                .add("certificateType", certificateType).add("certificate", certificate)
                .add("oldMemberCode", oldMemberCode).add("effectiveFlag", effectiveFlag).add("companyId", companyId)
                .add("status", status).add("storeIds", storeIds).add("hasWeixinAccount", hasWeixinAccount)
                .add("storeRFM", storeRFM).add("comRFM", comRFM).add("totalScore", totalScore)
                .add("cardStore", cardStore).add("iconUrl", iconUrl).toString();
    }

}
