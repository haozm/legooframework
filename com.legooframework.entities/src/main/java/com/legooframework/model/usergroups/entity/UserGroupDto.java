package com.legooframework.model.usergroups.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

public class UserGroupDto {

    private final Long groupId;
    private final int groupChannel;
    private final int groupType;
    private final String groupName;
    private final Long storeId;
    private final Set<MemberDto> members = Sets.newHashSet();

    public UserGroupDto(Long groupId, int groupChannel, int groupType, String groupName, Long storeId) {
        super();
        this.groupId = groupId;
        this.groupChannel = groupChannel;
        this.groupType = groupType;
        this.groupName = groupName;
        this.storeId = storeId;
    }

    public void addMember(MemberDto member) {
        this.members.add(member);
    }

    public void addMembers(Collection<? extends MemberDto> members) {
        this.members.addAll(members);
    }

    public Long getGroupId() {
        return groupId;
    }

    public int getGroupChannel() {
        return groupChannel;
    }

    public int getGroupType() {
        return groupType;
    }

    public String getGroupName() {
        return groupName;
    }

    public Set<MemberDto> getMembers() {
        return members;
    }

    public String getFullName() {
        String prefix = groupType == 1 ? "[会员]" : "[微信]";
        return String.format("%s%s", prefix, getGroupName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroupDto that = (UserGroupDto) o;
        return groupChannel == that.groupChannel &&
                groupType == that.groupType &&
                Objects.equal(groupId, that.groupId) &&
                Objects.equal(groupName, that.groupName) &&
                Objects.equal(storeId, that.storeId) &&
                Objects.equal(members, that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(groupId, groupChannel, groupType, groupName, storeId, members);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("groupId", groupId)
                .add("groupChannel", groupChannel)
                .add("groupType", groupType)
                .add("groupName", groupName)
                .add("storeId", storeId)
                .add("members", members)
                .toString();
    }

    public Long getStoreId() {
        return storeId;
    }

}
