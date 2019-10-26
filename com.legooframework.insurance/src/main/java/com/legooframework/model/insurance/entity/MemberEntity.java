package com.legooframework.model.insurance.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.joda.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 会员基本资料录入记录
 */
public class MemberEntity extends BaseEntity<Integer> {

    private String name, phone, mobile, familyAddr, workAddr, cardId, email;
    private LocalDate birthday;
    private int sexType, educationType;
    private int height, weight;

    public MemberEntity(String name, String cardId, String phone, String mobile, LocalDate birthday, int sex, int education,
                        int height, int weight, String familyAddr, String workAddr, String email) {
        super(UUID.randomUUID().toString().hashCode());
        this.name = name;
        this.phone = phone;
        this.mobile = mobile;
        this.familyAddr = familyAddr;
        this.workAddr = workAddr;
        this.cardId = cardId;
        this.birthday = birthday;
        this.sexType = sex;
        this.educationType = education;
        this.height = height;
        this.weight = weight;
        this.email = email;
    }

    MemberEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.phone = ResultSetUtil.getOptString(res, "phone", null);
            this.mobile = ResultSetUtil.getOptString(res, "mobile", null);
            this.familyAddr = ResultSetUtil.getOptString(res, "familyAddr", null);
            this.workAddr = ResultSetUtil.getOptString(res, "workAddr", null);
            this.name = ResultSetUtil.getString(res, "name");
            this.cardId = ResultSetUtil.getString(res, "cardId");
            Date _date = res.getDate("birthday");
            this.birthday = null == _date ? null : LocalDate.fromDateFields(_date);
            this.sexType = ResultSetUtil.getObject(res, "sexType", Integer.class);
            this.educationType = ResultSetUtil.getObject(res, "educationType", Integer.class);
            this.height = ResultSetUtil.getOptObject(res, "height", Integer.class).orElse(0);
            this.weight = ResultSetUtil.getOptObject(res, "weight", Integer.class).orElse(0);
            this.email = ResultSetUtil.getOptString(res, "email", null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore MemberEntity has SQLException", e);
        }
    }

    Optional<MemberEntity> change(String name, String cardId, String phone, String mobile, LocalDate birthday, int sex,
                                  int education, int height, int weight, String familyAddr, String workAddr, String email) {
        MemberEntity clone = (MemberEntity) cloneMe();
        clone.name = name;
        clone.phone = phone;
        clone.mobile = mobile;
        clone.familyAddr = familyAddr;
        clone.workAddr = workAddr;
        clone.cardId = cardId;
        clone.birthday = birthday;
        clone.sexType = sex;
        clone.educationType = education;
        clone.height = height;
        clone.weight = weight;
        clone.email = email;
        if (this.equals(clone)) return Optional.empty();
        return Optional.of(clone);
    }

    String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberEntity)) return false;
        MemberEntity that = (MemberEntity) o;
        return sexType == that.sexType &&
                educationType == that.educationType &&
                height == that.height &&
                weight == that.weight &&
                Objects.equal(getId(), that.getId()) &&
                Objects.equal(name, that.name) &&
                Objects.equal(phone, that.phone) &&
                Objects.equal(mobile, that.mobile) &&
                Objects.equal(familyAddr, that.familyAddr) &&
                Objects.equal(workAddr, that.workAddr) &&
                Objects.equal(cardId, that.cardId) &&
                Objects.equal(email, that.email) &&
                Objects.equal(birthday, that.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), name, phone, mobile, familyAddr, workAddr, cardId, birthday, sexType,
                educationType, height, weight, email);
    }

    public String getPhone() {
        return phone;
    }

    public String getMobile() {
        return mobile;
    }

    public String getFamilyAddr() {
        return familyAddr;
    }

    public String getWorkAddr() {
        return workAddr;
    }

    public String getCardId() {
        return cardId;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public int getSexType() {
        return sexType;
    }

    public int getEducationType() {
        return educationType;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("name", name);
        params.put("phone", phone);
        params.put("mobile", mobile);
        params.put("familyAddr", familyAddr);
        params.put("workAddr", workAddr);
        params.put("cardId", cardId);
        params.put("birthday", birthday.toDate());
        params.put("sexType", sexType);
        params.put("educationType", educationType);
        params.put("height", height);
        params.put("weight", weight);
        params.put("email", email);
        return params;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", getId());
        params.put("name", name);
        params.put("phone", phone);
        params.put("mobile", mobile);
        params.put("familyAddr", familyAddr);
        params.put("workAddr", workAddr);
        params.put("cardId", cardId);
        params.put("birthday", birthday.toString("yyyy-MM-dd"));
        params.put("sexType", sexType);
        params.put("educationType", educationType);
        params.put("height", height);
        params.put("weight", weight);
        params.put("email", email);
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("phone", phone)
                .add("mobile", mobile)
                .add("familyAddr", familyAddr)
                .add("workAddr", workAddr)
                .add("cardId", cardId)
                .add("birthday", birthday)
                .add("sexType", sexType)
                .add("educationType", educationType)
                .add("height", height)
                .add("weight", weight)
                .add("email", email)
                .toString();
    }
}
