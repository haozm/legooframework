package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CirclePermissionEntity extends BaseEntity<String> {

    private PermissionType permission;
    private Set<String> blockWxIds;

    boolean hasBlockList() {
        return CollectionUtils.isNotEmpty(blockWxIds);
    }

    public CirclePermissionEntity(String wxId, int permission, String blockWxId) {
        super(wxId);
        if (permission == 207) {
            this.permission = PermissionType.PERMITED;
            if (StringUtils.isNotEmpty(blockWxId)) this.blockWxIds = Sets.newHashSet(blockWxId);
        } else {
            this.permission = PermissionType.parse(permission);
        }
    }

    Optional<CirclePermissionEntity> change(CirclePermissionEntity that) {
        CirclePermissionEntity clone = (CirclePermissionEntity) cloneMe();
        clone.permission = that.permission;
        if (CollectionUtils.isNotEmpty(that.blockWxIds)) {
            clone.blockWxIds = CollectionUtils.isEmpty(this.blockWxIds) ? Sets.newHashSet() :
                    Sets.newHashSet(this.blockWxIds);
            clone.blockWxIds.addAll(that.blockWxIds);
        }
        return this.equals(clone) ? Optional.empty() : Optional.of(clone);
    }

    String getWinxinId() {
        return super.getId();
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("blockWxIds");
        params.put("permission", permission.getPermission());
        params.put("blockWxIds", CollectionUtils.isEmpty(blockWxIds) ? null : StringUtils.join(blockWxIds, ','));
        return params;
    }

    CirclePermissionEntity(String wxId, ResultSet res) {
        super(wxId);
        try {
            Long _permission = ResultSetUtil.getObject(res, "permission", Long.class);
            this.permission = PermissionType.parse(_permission.intValue());
            String _blockWxIds = res.getString("blockWxIds");
            if (!Strings.isNullOrEmpty(_blockWxIds)) {
                this.blockWxIds = Sets.newHashSet(StringUtils.split(_blockWxIds, ','));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore CirclePermissionEntity has SQLException", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CirclePermissionEntity that = (CirclePermissionEntity) o;
        return permission == that.permission &&
                Objects.equal(this.getId(), that.getId()) &&
                SetUtils.isEqualSet(blockWxIds, that.blockWxIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.getId(), permission, blockWxIds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("weixinId", getWinxinId())
                .add("permission", permission)
                .add("blockWxIds", blockWxIds)
                .toString();
    }
}
