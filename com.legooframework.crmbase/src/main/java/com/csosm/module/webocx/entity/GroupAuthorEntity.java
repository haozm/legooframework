package com.csosm.module.webocx.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Map;

public class GroupAuthorEntity extends BaseEntity<Integer> {

    private Integer companyId;
    private String groupId;
    private boolean enabled;

    GroupAuthorEntity(StoreEntity store, PageDefinedDto pageDefinedDto, boolean enabled,
                      LoginUserContext userContext) {
        super(store.getId(), userContext.getUserId(), DateTime.now().toDate());
        this.companyId = store.getCompanyId().or(-1);
        this.groupId = pageDefinedDto.getFullName();
        this.enabled = enabled;
    }

    GroupAuthorEntity(OrganizationEntity company, PageDefinedDto pageDefinedDto, boolean enabled,
                      LoginUserContext userContext) {
        super(-1, userContext.getUserId(), DateTime.now().toDate());
        this.companyId = company.getId();
        this.groupId = pageDefinedDto.getFullName();
        this.enabled = enabled;
    }

    GroupAuthorEntity(Integer id, Integer companyId, String groupId, boolean enabled,
                      Object createUserId, Date createTime,
                      Object editor, Date editTime) {
        super(id, createUserId, createTime, editor, editTime);
        this.companyId = companyId;
        this.groupId = groupId;
        this.enabled = enabled;
    }

    Optional<GroupAuthorEntity> change(boolean enabled, LoginUserContext userContext) {
        if (this.enabled == enabled) return Optional.absent();
        try {
            GroupAuthorEntity clone = (GroupAuthorEntity) this.clone();
            clone.enabled = enabled;
            clone.setModifyUserId(userContext.getUserId());
            return Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDisAnabled() {
        return !enabled;
    }

    public boolean equalsPage(PageDefinedDto pg) {
        return StringUtils.equals(this.groupId, pg.getFullName());
    }

    public boolean equalsId(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupAuthorEntity)) return false;
        if (!super.equals(o)) return false;
        GroupAuthorEntity that = (GroupAuthorEntity) o;
        return Objects.equal(companyId, that.companyId) &&
                Objects.equal(groupId, that.groupId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupAuthorEntity)) return false;
        if (!super.equals(o)) return false;
        GroupAuthorEntity that = (GroupAuthorEntity) o;
        return enabled == that.enabled &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), companyId, groupId, enabled);
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("companyId", companyId);
        map.put("groupId", groupId);
        map.put("enabled", enabled ? 1 : 0);
        return map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("storeId", getId())
                .add("companyId", companyId)
                .add("groupId", groupId)
                .add("enabled", enabled)
                .add("super", super.toString())
                .toString();
    }
}
