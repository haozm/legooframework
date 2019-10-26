package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class MemberEntity extends BaseEntity<Integer> implements ToReplace {

    private final String name, phone, cardNum, cardName, companyName;
    private final Integer shoppingGuideId, storeId, companyId, orgId;
    private final boolean effective;

    private final int calendarType;
    private final LocalDate birthday;
    private final LocalDate thisYearBirthday;
    private final double totalScore, rechargeAmount;

    MemberEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.name = res.getString("name");
            this.phone = res.getString("phone");
            this.storeId = res.getInt("real_storeId");
            this.orgId = res.getInt("organization_id");
            this.shoppingGuideId = res.getInt("real_shoppingguide_id");
            this.companyId = res.getInt("company_id");
            this.effective = res.getInt("status") == 1;
            this.cardNum = res.getString("memberCardNum");
            this.cardName = res.getString("cardName");
            this.companyName = res.getString("companyName");
            this.calendarType = res.getInt("calendarType");
            this.totalScore = res.getBigDecimal("totalScore") == null ? 0.0D : res.getBigDecimal("totalScore").doubleValue();
            this.rechargeAmount = res.getBigDecimal("rechargeAmount") == null ? 0.0D : res.getBigDecimal("rechargeAmount").doubleValue();
            this.birthday = res.getObject("birthday") == null ? null : LocalDate.fromDateFields(res.getDate("birthday"));
            this.thisYearBirthday = res.getObject("thisYearBirthday") == null ? null :
                    DateTimeUtils.parseShortYYYYMMDD(res.getString("thisYearBirthday"));
        } catch (SQLException e) {
            throw new RuntimeException("Restore MemberEntity has SQLException", e);
        }
    }

    public Optional<LocalDate> getBirthday() {
        return Optional.ofNullable(birthday);
    }

    public Optional<LocalDate> getThisYearBirthday() {
        return Optional.ofNullable(thisYearBirthday);
    }

    public int getCalendarType() {
        return calendarType;
    }

    public Optional<Integer> getShoppingGuideId() {
        return Optional.ofNullable(shoppingGuideId <= 0 ? null : shoppingGuideId);
    }

    public boolean hasPhone() {
        return this.phone != null && this.phone.length() == 11 && NumberUtils.isDigits(this.phone);
    }

    boolean isEffective() {
        return effective;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public String getPhone() {
        return phone;
    }

    public Optional<String> getOptPhone() {
        return Optional.ofNullable(phone);
    }

    public Integer getStoreId() {
        return storeId;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("shoppingGuideId", shoppingGuideId);
        params.put("memberId", getId());
        params.put("calendarType", calendarType);
        params.put("birthday", birthday == null ? new LocalDate(1970, 1, 1).toDate() : birthday.toDate());
        if (thisYearBirthday == null) {
            LocalDate now = LocalDate.now();
            params.put("thisYearBirthday", new LocalDate(now.getYear(), 1, 1).toDate());
        } else {
            params.put("thisYearBirthday", thisYearBirthday.toDate());
        }
        return params;
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("会员姓名", Strings.nullToEmpty(this.name));
        params.put("会员电话", Strings.nullToEmpty(this.phone));
        params.put("会员卡号", Strings.nullToEmpty(this.cardNum));
        params.put("当前积分", this.totalScore);
        params.put("卡片类别", Strings.isNullOrEmpty(cardName) ? "--" : this.cardName);
        params.put("储值余额", this.rechargeAmount);
        params.put("品牌名称", this.companyName);
        params.put("会员生日", this.birthday == null ? "未指定" : this.birthday.toString("yyyy-MM-dd"));
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberEntity that = (MemberEntity) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(getId(), that.getId()) &&
                Objects.equal(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), name, phone);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("phone", phone)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("calendarType", calendarType)
                .add("birthday", birthday)
                .toString();
    }
}
