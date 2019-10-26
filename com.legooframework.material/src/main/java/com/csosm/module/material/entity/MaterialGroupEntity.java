package com.csosm.module.material.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

public class MaterialGroupEntity extends BaseEntity<Integer> {

    private final String groupName;
    private final int groupType;

    MaterialGroupEntity(Integer id, String groupName, int groupType) {
        super(id);
        this.groupName = groupName;
        this.groupType = groupType;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isMaterial() {
        return groupType == 1;
    }

    public int getGroupType() {
        return groupType;
    }

    public boolean isHuaSu() {
        return groupType == 2;
    }

    public boolean isShanPin() {
        return groupType == 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MaterialGroupEntity that = (MaterialGroupEntity) o;
        return Objects.equal(groupName, that.groupName) &&
                groupType == that.groupType;
    }

    public Map<String, Object> toViewBean() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("groupId", this.getId());
        map.put("groupName", this.getGroupName());
        return map;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), groupName, groupType);
    }

	@Override
	public String toString() {
		return "MaterialGroupEntity [groupName=" + groupName + ", groupType=" + groupType + "]";
	}

   
}
