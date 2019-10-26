package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.*;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class WxMsgWhiteListEntity extends BaseEntity<Integer> {

    private boolean prohibit;
    private Set<Integer> incloudIds, excloudIds;
    private Integer storeId, companyId;

    WxMsgWhiteListEntity(EmployeeEntity employee, StoreEntity store) {
        super(0);
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(store.getCompanyId().isPresent());
        this.prohibit = true;
        this.incloudIds = Sets.newHashSet();
        this.incloudIds.add(employee.getId());
        this.storeId = store.getId();
        this.companyId = store.getCompanyId().get();
    }

    WxMsgWhiteListEntity(StoreEntity store, boolean prohibit) {
        super(0);
        Preconditions.checkNotNull(store);
        Preconditions.checkArgument(store.getCompanyId().isPresent());
        this.prohibit = prohibit;
        this.incloudIds = null;
        this.storeId = store.getId();
        this.companyId = store.getCompanyId().get();
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        data.put("prohibitTag", prohibit ? 1 : 0);
        data.put("storeId", storeId);
        data.put("companyId", companyId);
        data.put("incloudIds", CollectionUtils.isEmpty(incloudIds) ? null : Joiner.on(',').join(incloudIds));
        data.put("excloudIds", CollectionUtils.isEmpty(excloudIds) ? null : Joiner.on(',').join(excloudIds));
        return data;
    }

    WxMsgWhiteListEntity(Integer id, Object createUserId, Date createTime, ResultSet res) {
        super(id, createUserId, createTime);
        try {
            this.prohibit = res.getInt("prohibitTag") == 1;
            String in_ids = res.getString("incloudIds");
            if (StringUtils.isNoneEmpty(in_ids)) {
                this.incloudIds = Sets.newHashSet();
                for (String $it : StringUtils.split(in_ids, ',')) this.incloudIds.add(Integer.valueOf($it));
            }
            String ex_ids = res.getString("excloudIds");
            if (StringUtils.isNoneEmpty(ex_ids)) {
                this.excloudIds = Sets.newHashSet();
                for (String $it : StringUtils.split(ex_ids, ',')) this.excloudIds.add(Integer.valueOf($it));
            }
            this.storeId = res.getInt("storeId");
            this.companyId = res.getInt("companyId");
        } catch (SQLException e) {
            throw new RuntimeException("Restore LabelNodeEntity has SQLException", e);
        }
    }

    Optional<WxMsgWhiteListEntity> open() {
        if (!isProhibit()) return Optional.absent();
        try {
            WxMsgWhiteListEntity clone = (WxMsgWhiteListEntity) super.clone();
            clone.prohibit = false;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Optional<WxMsgWhiteListEntity> close() {
        if (isProhibit()) return Optional.absent();
        try {
            WxMsgWhiteListEntity clone = (WxMsgWhiteListEntity) super.clone();
            clone.prohibit = true;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Optional<WxMsgWhiteListEntity> addWithList(EmployeeEntity employee) {
        Preconditions.checkNotNull(employee);
        try {
            if (null == this.incloudIds) {
                WxMsgWhiteListEntity clone = (WxMsgWhiteListEntity) super.clone();
                Set<Integer> incloudIds = Sets.newHashSet();
                incloudIds.add(employee.getId());
                clone.incloudIds = incloudIds;
                return Optional.of(clone);
            } else {
                if (this.incloudIds.contains(employee.getId())) return Optional.absent();
                WxMsgWhiteListEntity clone = (WxMsgWhiteListEntity) super.clone();
                Set<Integer> incloudIds = Sets.newHashSet(this.incloudIds);
                incloudIds.add(employee.getId());
                clone.incloudIds = incloudIds;
                return Optional.of(clone);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Optional<WxMsgWhiteListEntity> removeWithList(EmployeeEntity employee) {
        Preconditions.checkNotNull(employee);
        if (null == this.incloudIds || !this.incloudIds.contains(employee.getId()))
            return Optional.absent();
        try {
            WxMsgWhiteListEntity clone = (WxMsgWhiteListEntity) super.clone();
            Set<Integer> incloudIds = Sets.newHashSet(this.incloudIds);
            incloudIds.remove(employee.getId());
            clone.incloudIds = incloudIds;
            return Optional.of(clone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isProhibit() {
        return prohibit;
    }

    public Optional<Set<Integer>> getIncloudIds() {
        return Optional.fromNullable(CollectionUtils.isEmpty(incloudIds) ? null : incloudIds);
    }

    public Optional<Set<Integer>> getExcloudIds() {
        return Optional.fromNullable(CollectionUtils.isEmpty(excloudIds) ? null : excloudIds);
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public boolean isPassEmployee(EmployeeEntity employee) {
        Preconditions.checkNotNull(employee);
        if (!isProhibit()) return true;
        if (CollectionUtils.isEmpty(this.incloudIds)) return false;
        return isProhibit() && this.incloudIds.contains(employee.getId());
    }

    public boolean isPassLoginUser(LoginUserContext user) {
        Preconditions.checkNotNull(user);
        if (!isProhibit()) return true;
        if (CollectionUtils.isEmpty(this.incloudIds)) return false;
        return isProhibit() && this.incloudIds.contains(user.getUserId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WxMsgWhiteListEntity that = (WxMsgWhiteListEntity) o;
        return prohibit == that.prohibit &&
                SetUtils.isEqualSet(incloudIds, that.incloudIds) &&
                SetUtils.isEqualSet(excloudIds, that.excloudIds) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(prohibit, incloudIds, excloudIds, storeId, companyId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("prohibit", prohibit)
                .add("incloudIds", incloudIds)
                .add("excloudIds", excloudIds)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .toString();
    }
}
