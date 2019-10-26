package com.legooframework.model.covariant.entity;


import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;


public class OrgEntity extends BaseEntity<Integer> implements ToReplace {

    private final String code, name, shortName;
    private final Integer type, parentId, depth;

    OrgEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.parentId = ResultSetUtil.getObject(res, "parentId", Integer.class);
            this.depth = ResultSetUtil.getObject(res, "depth", Integer.class);
            this.type = ResultSetUtil.getObject(res, "type", Integer.class);
            this.code = ResultSetUtil.getObject(res, "code", String.class);
            this.name = ResultSetUtil.getObject(res, "name", String.class);
            if (isCompany()) {
                this.shortName = ResultSetUtil.getOptString(res, "shortName", "");
            } else {
                this.shortName = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore OrganizationEntity has SQLException", e);
        }
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        if (isCompany()) {
            params.put("orgId", getId());
        } else {
            params.put("companyId", getId());
        }
        params.put("shortName", shortName);
        return params;
    }


    boolean isCompany() {
        return 1 == this.type;
    }

    boolean isDirectSupervisor(OrgEntity child) {
        if (child.isCompany()) return false;
        String sub_code = StringUtils.substring(child.code, 0, StringUtils.lastIndexOf(child.code, '_'));
        return StringUtils.equals(this.code, sub_code);
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = Maps.newHashMap();
        if (isCompany()) {
            params.put("公司名称", Strings.nullToEmpty(this.name));
            params.put("品牌名称", Strings.nullToEmpty(this.name));
        } else {
            params.put("机构名称", Strings.nullToEmpty(this.name));
        }
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrgEntity that = (OrgEntity) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(depth, that.depth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), code, name, type, parentId, depth);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("name", name)
                .add("type", type)
                .add("parentId", parentId)
                .add("depth", depth)
                .toString();
    }
}
