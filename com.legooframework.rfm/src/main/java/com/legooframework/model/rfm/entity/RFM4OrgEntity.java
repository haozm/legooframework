package com.legooframework.model.rfm.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.Date;
import java.util.Map;

public class RFM4OrgEntity extends BaseEntity<Long> {

    private Integer companyId, storeId, orgId;
    private RVal rVal;
    private FVal fVal;
    private MVal mVal;
    private final int valType;

    RFM4OrgEntity(LoginUserContext user, OrganizationEntity com, RVal rVal, FVal fVal, MVal mVal, int valType) {
        super(0L, user.getUserId(), new Date());
        this.companyId = com.isCompany() ? com.getId() : com.getMyCompanyId();
        this.orgId = com.isCompany() ? -1 : com.getId();
        this.storeId = -1;
        this.rVal = rVal;
        this.fVal = fVal;
        this.mVal = mVal;
        this.valType = valType;
    }

    RFM4OrgEntity(LoginUserContext user, StoreEntity store, RVal rVal, FVal fVal, MVal mVal, int valType) {
        super(0L, user.getUserId(), new Date());
        this.storeId = store.getId();
        this.orgId = -1;
        this.companyId = store.getCompanyId().orNull();
        this.rVal = rVal;
        this.fVal = fVal;
        this.mVal = mVal;
        this.valType = valType;
    }

    RFM4OrgEntity(Object createUserId, Date createTime, Integer companyId, Integer orgId, Integer storeId, int valType,
                  int r1, int r2, int r3, int r4,
                  int f1, int f2, int f3, int f4,
                  int m1, int m2, int m3, int m4) {
        super(0L, createUserId, createTime);
        this.companyId = companyId;
        this.storeId = storeId;
        this.orgId = orgId;
        this.rVal = new RVal(r1, r2, r3, r4);
        this.fVal = new FVal(f1, f2, f3, f4);
        this.mVal = new MVal(m1, m2, m3, m4);
        this.valType = valType;
    }

    public Integer getStoreId() {
        Preconditions.checkState(isStoreRFM(), "当前取值为公司RFM,无法获取门店设定值...");
        return storeId;
    }

    public boolean isCompanyRFM() {
        return -1 == storeId && -1 == orgId;
    }

    public boolean isOrgRFM() {
        return -1 == storeId && -1 != orgId;
    }

    public boolean isStoreRFM() {
        return -1 != storeId && -1 == orgId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public RVal getrVal() {
        return rVal;
    }

    public FVal getfVal() {
        return fVal;
    }

    public MVal getmVal() {
        return mVal;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public RFM4OrgEntity changeRFM(int r1, int r2, int r3, int r4,
                                   int f1, int f2, int f3, int f4,
                                   int m1, int m2, int m3, int m4,
                                   LoginUserContext loginUser) {
        try {
            RFM4OrgEntity clone = (RFM4OrgEntity) super.clone();
            clone.rVal = new RVal(r1, r2, r3, r4);
            clone.fVal = new FVal(f1, f2, f3, f4);
            clone.mVal = new MVal(m1, m2, m3, m4);
            clone.setModifyUserId(loginUser.getUserId());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> toView() {
        return this.toViewMap();
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> param = super.toMap();
        param.put("companyId", companyId);
        param.put("storeId", storeId);
        param.put("orgId", orgId);
        param.putAll(rVal.toMap());
        param.putAll(fVal.toMap());
        param.putAll(mVal.toMap());
        param.put("type", valType);
        return param;
    }
    
    public Map<String, Object> toViewMap() {
        Map<String, Object> param = super.toMap();
        param.put("companyId", companyId);
        param.put("storeId", storeId);
        param.put("orgId", orgId);
        param.putAll(rVal.toViewMap());
        param.putAll(fVal.toMap());
        param.putAll(mVal.toMap());
        param.put("type", valType);
        return param;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RFM4OrgEntity that = (RFM4OrgEntity) o;
        return valType == that.valType &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(orgId, that.orgId) &&
                Objects.equal(rVal, that.rVal) &&
                Objects.equal(fVal, that.fVal) &&
                Objects.equal(mVal, that.mVal);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(companyId, orgId, storeId, rVal, fVal, mVal, valType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("orgId", orgId)
                .add("storeId", storeId)
                .add("rVal", rVal)
                .add("fVal", fVal)
                .add("mVal", mVal)
                .toString();
    }
}
