package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;

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
    private Set<Integer> shoppingGuideIds;
    private final Integer companyId, storeId;

    CrmMemberEntity(Integer id, String name, String phoneNo, int birthdayType, String birthday,
                    int effectiveFlag, String shoppingGuideIds, Integer companyId, Integer storeId) {
        super(id);
        this.name = name;
        this.phoneNo = phoneNo;
        if (birthdayType != -1) {
            if (StringUtils.isNoneEmpty(birthday) && !StringUtils.equals("00-00", birthday)) {
                this.birthday = birthdayType == 1 ? new Birthday(1, birthday) : new Birthday(2, birthday);
            }
        }
        this.effectiveFlag = effectiveFlag == 1;
        if (!Strings.isNullOrEmpty(shoppingGuideIds)) {
            this.shoppingGuideIds = Sets.newHashSet();
            Stream.of(StringUtils.split(shoppingGuideIds, ',')).forEach(x -> this.shoppingGuideIds.add(Integer.valueOf(x)));
        }
        this.companyId = companyId;
        this.storeId = storeId;
    }

    public boolean isCrossStore(CrmStoreEntity store) {
        return !this.storeId.equals(store.getId());
    }

    public String getName() {
        return name;
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
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, phoneNo, birthday, effectiveFlag, companyId, storeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("phoneNo", phoneNo)
                .add("birthday", birthday)
                .add("effectiveFlag", effectiveFlag)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .toString();
    }
}
