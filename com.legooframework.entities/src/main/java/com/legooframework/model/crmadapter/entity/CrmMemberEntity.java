package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CrmMemberEntity extends BaseEntity<Integer> {
    private String name;
    private String phoneNo;
    private Birthday birthday;
    // 有效标准 2：无效，其他有效
    private boolean effectiveFlag;
    private Integer status;
    private LocalDate lastVisitTime;
    private Set<Integer> shoppingGuideIds;
    private final Integer companyId, storeId;

    CrmMemberEntity(Integer id, ResultSet res) {
        super(id, res);
        try {
            long calendarType = ResultSetUtil.getOptObject(res, "birthdayType", Long.class).orElse(-1L);
            if (calendarType != -1) {
                String birthday_val = ResultSetUtil.getOptString(res, "birthday", null);
                if (StringUtils.isNoneEmpty(birthday_val) && !StringUtils.equals("00-00", birthday_val)) {
                    this.birthday = calendarType == 1 ? new Birthday(1, birthday_val) : new Birthday(2, birthday_val);
                }
            }
            this.name = ResultSetUtil.getOptString(res, "name", "无名");
            ResultSetUtil.getOptObject(res, "lastVisitTime", Date.class)
                    .ifPresent(_lastVisitTime -> this.lastVisitTime = LocalDate.fromDateFields(_lastVisitTime));

            String _shoppingGuideIds = ResultSetUtil.getOptString(res, "shoppingGuideIds", null);
            if (!Strings.isNullOrEmpty(_shoppingGuideIds)) {
                this.shoppingGuideIds = Sets.newHashSet();
                Stream.of(StringUtils.split(_shoppingGuideIds, ',')).forEach(x -> this.shoppingGuideIds.add(Integer.valueOf(x)));
            }

            this.phoneNo = ResultSetUtil.getOptString(res, "phoneNo", null);
            this.effectiveFlag = ResultSetUtil.getObject(res, "effectiveFlag", Long.class) != 2;
            this.status = ResultSetUtil.getObject(res, "status", Integer.class);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "storeId", Long.class).intValue();
        } catch (SQLException e) {
            throw new RuntimeException("Restore CrmOrganizationEntity has SQLException", e);
        }
    }

    public boolean isCrossStore(CrmStoreEntity store) {
        return !this.storeId.equals(store.getId());
    }

    public String getName() {
        return name;
    }

    public Optional<LocalDate> getLastVisitTime() {
        return Optional.ofNullable(lastVisitTime);
    }

    public Optional<String> getPhoneNo() {
        return Optional.ofNullable(phoneNo);
    }

    public Optional<Birthday> getBirthday() {
        return Optional.ofNullable(birthday);
    }

    public Optional<Set<Integer>> getShoppingGuideIds() {
        return Optional.ofNullable(shoppingGuideIds);
    }

    public boolean isEffectiveFlag() {
        return effectiveFlag;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrmMemberEntity that = (CrmMemberEntity) o;
        return effectiveFlag == that.effectiveFlag &&
                Objects.equals(name, that.name) &&
                Objects.equals(phoneNo, that.phoneNo) &&
                Objects.equals(birthday, that.birthday) &&
                Objects.equals(status, that.status) &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, phoneNo, birthday,
                effectiveFlag, status, companyId, storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("phoneNo", phoneNo)
                .add("birthday", birthday)
                .add("effectiveFlag", effectiveFlag)
                .add("status", status)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .toString();
    }
}
