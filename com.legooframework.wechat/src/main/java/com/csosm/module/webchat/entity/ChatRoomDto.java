package com.csosm.module.webchat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

public class ChatRoomDto {
    private final String name;
    private final String nickname;

    ChatRoomDto(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> toTreeNode() {
        Map<String, String> map = Maps.newHashMap();
        map.put("id", name);
        map.put("label", nickname);
        return map;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoomDto that = (ChatRoomDto) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, nickname);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("nickname", nickname)
                .toString();
    }
}
