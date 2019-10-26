package com.legooframework.model.crmadapter.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.util.Optional;

public class CrmMemberEntity extends BaseEntity<Integer> {

    private final String name, phoneNo, storeName, companyName, shoppingGuideName;
    private final Birthday birthday;
    private final Integer companyId, storeId, shoppingGuideId;

    CrmMemberEntity(Integer id, String nm, String ph, int bdt, String bd, String lbd,
                    Integer cId, String cnm, Integer sId, String snm, Integer gId, String gnm) {
        super(id);
        this.name = nm;
        this.phoneNo = ph;
        this.companyId = cId;
        this.companyName = cnm;

        this.storeId = sId;
        this.storeName = snm;

        this.shoppingGuideId = gId;
        this.shoppingGuideName = gnm;

        if (bdt == 1) {
            this.birthday = Birthday.createBirthday(bd);
        } else if (bdt == 2) {
            this.birthday = Birthday.createBirthday(lbd);
        } else {
            this.birthday = null;
        }
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getShoppingGuideName() {
        return shoppingGuideName;
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

    public Optional<Integer> getShoppingGuideId() {
        return Optional.ofNullable(shoppingGuideId);
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
        if (!(o instanceof CrmMemberEntity)) return false;
        CrmMemberEntity that = (CrmMemberEntity) o;
        return Objects.equal(getId(), that.getId()) &&
                Objects.equal(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.getId(), companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("phoneNo", phoneNo)
                .add("storeName", storeName)
                .add("companyName", companyName)
                .add("shoppingGuideName", shoppingGuideName)
                .add("birthday", birthday)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("shoppingGuideId", shoppingGuideId)
                .toString();
    }
}
