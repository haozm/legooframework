package com.csosm.module.webchat.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ChatRoomContactEntity extends BaseEntity<String> {

    private final String nickname;
    private final List<String> owners;
    private final List<Member> members;
    private final int size;
    private final Integer companyId;
    private final List<Integer> storeIds;

    ChatRoomContactEntity(String name, String nickname, String owners, String[] weixinIds,
                          String[] weixinNames, String storeIds, Integer companyId) {
        super(name);
        this.nickname = nickname;
        this.owners = Lists.newArrayList(StringUtils.split(owners, ','));
        if (ArrayUtils.isNotEmpty(weixinIds)) {
            // Preconditions.checkArgument(weixinIds.length == weixinNames.length, "微信ID与微信昵称数量不匹配...");
            this.members = Lists.newArrayList();
            for (int i = 0; i < weixinIds.length; i++) {
                this.members.add(new Member(weixinIds[i], null));
            }
            this.size = members.size();
        } else {
            this.members = null;
            this.size = 0;
        }
        String[] ids = StringUtils.split(storeIds, ',');
        this.storeIds = Lists.newArrayList();
        for (String id : ids) this.storeIds.add(Integer.valueOf(id));
        this.companyId = companyId;
    }

    public ChatRoomDto toChatRoomDto() {
        return new ChatRoomDto(getId(), nickname);
    }


    public String getNickname() {
        return nickname;
    }

    public List<String> getOwners() {
        return owners;
    }

    public List<Integer> getStoreIds() {
        return storeIds;
    }

    public List<Member> getMembers() {
        return members;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoomContactEntity that = (ChatRoomContactEntity) o;
        return Objects.equal(getId(), that.getId());
    }


    public Integer getCompanyId() {
        return companyId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nickname", nickname)
                .add("owners", owners)
                .add("members", members)
                .add("size", size)
                .add("companyId", companyId)
                .add("storeIds", storeIds)
                .toString();
    }

    public class Member {
        private final String name;
        private final String nickname;

        Member(String name, String nickname) {
            this.name = name;
            this.nickname = nickname;
        }

        public String getName() {
            return name;
        }

        public String getNickname() {
            return nickname;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Member member = (Member) o;
            return Objects.equal(name, member.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .add("nickname", nickname)
                    .toString();
        }
    }
}
