package com.legooframework.model.dict.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.Sorting;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.dict.dto.KvDictDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class KvDictEntity extends BaseDictEnity implements Sorting {

    private String value, name, desc;
    private int index;


    KvDictEntity(int id, ResultSet res) {
        super(id, res);
        try {
            this.value = res.getString("value");
            this.name = res.getString("name");
            this.desc = res.getString("desc");
            this.index = res.getInt("index");
        } catch (SQLException e) {
            throw new RuntimeException("Restore KvDictEntity has SQLException", e);
        }
    }

    KvDictEntity(String type, LoginContext loginContext, String value,
                 String name, String desc, int index) {
        super(type, loginContext.getTenantId(), loginContext.getLoginId());
        this.value = value;
        this.name = name;
        this.desc = desc;
        this.index = index;
    }

    public KvDictEntity edit(String name, String desc, int index) {
        KvDictEntity clone = (KvDictEntity) cloneMe();
        clone.name = name;
        clone.desc = desc;
        clone.index = index;
        return clone;
    }


    public KvDictDto createDto() {
        return new KvDictDto(value, name, index, getType());
    }

    @Override
    public boolean equalsEntity(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        KvDictEntity that = (KvDictEntity) o;
        return Objects.equal(getValue(), that.getType()) &&
                Objects.equal(value, that.value) &&
                Objects.equal(name, that.name);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;
        KvDictEntity that = (KvDictEntity) o;
        return index == that.index &&
                Objects.equal(value, that.value) &&
                Objects.equal(name, that.name) &&
                Objects.equal(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), value, name, desc, index);
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("tenantId", getTenantId())
                .add("type", getType())
                .add("value", value)
                .add("name", name)
                .add("desc", desc)
                .add("index", index)
                .toString();
    }
}
