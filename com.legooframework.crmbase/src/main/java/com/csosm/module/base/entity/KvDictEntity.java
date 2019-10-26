package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class KvDictEntity extends BaseEntity<Integer> {

    private String key, value, desc;
    private final String type;
    private int index;

    KvDictEntity(Integer id, String key, String value, String desc, String type, int index) {
        super(id);
        this.key = key;
        this.value = value;
        this.desc = desc;
        this.type = type;
        this.index = index;
    }


    public String getValue() {
        return value;
    }


    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KvDictEntity that = (KvDictEntity) o;
        return index == that.index &&
                Objects.equal(value, that.value) &&
                Objects.equal(key, that.key) &&
                Objects.equal(desc, that.desc) &&
                Objects.equal(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, key, desc, type, index);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("value", value)
                .add("desc", desc)
                .add("type", type)
                .add("index", index)
                .toString();
    }
}
