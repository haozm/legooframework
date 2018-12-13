package com.legooframework.model.crmadapter.entity;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CrmOrganizationEntity extends BaseEntity<Integer> {

    private String code;
    private Integer parentId;
    // 1 com  2 org
    private Integer type;
    private String name;
    private String shortName;
    private Integer status;
    private boolean rootNode;

    CrmOrganizationEntity(Integer id, ResultSet res) {
        super(id, res);
        try {
            this.code = ResultSetUtil.getString(res, "code");
            this.parentId = ResultSetUtil.getObject(res, "parentId", Integer.class);
            this.type = ResultSetUtil.getObject(res, "type", Integer.class);
            this.name = ResultSetUtil.getString(res, "name");
            this.shortName = ResultSetUtil.getString(res, "shortName");
            this.status = ResultSetUtil.getObject(res, "status", Integer.class);
        } catch (SQLException e) {
            throw new RuntimeException("Restore CrmOrganizationEntity has SQLException", e);
        }
    }

    public Object getParentId() {
        return parentId;
    }

    public boolean isCompany() {
        return Objects.equal(1, type);
    }

    public boolean isDept() {
        return Objects.equal(2, type);
    }

    public boolean isStore() {
        return Objects.equal(3, type);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getShortName() {
        return Optional.ofNullable(shortName);
    }

    public Integer getStatus() {
        return status;
    }

    public boolean isRootNode() {
        return rootNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrmOrganizationEntity that = (CrmOrganizationEntity) o;
        return rootNode == that.rootNode
                && Objects.equal(code, that.code)
                && Objects.equal(parentId, that.parentId)
                && Objects.equal(type, that.type)
                && Objects.equal(name, that.name)
                && Objects.equal(shortName, that.shortName)
                && Objects.equal(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                super.hashCode(), code, parentId, type, name, shortName, status, rootNode);
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("code", code)
                .add("parentId", parentId)
                .add("type", type)
                .add("name", name)
                .add("shortName", shortName)
                .add("status", status)
                .add("rootNode", rootNode)
                .toString();
    }
}
