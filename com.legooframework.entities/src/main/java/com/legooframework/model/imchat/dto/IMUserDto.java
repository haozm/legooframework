package com.legooframework.model.imchat.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

public class IMUserDto {

    private final String avatar, id, username, sign, status;

    public IMUserDto(String avatar, String id, String username, String sign) {
        this.avatar = Strings.isNullOrEmpty(avatar) ? "/static/images/user.jpg" : avatar;
        this.id = id;
        this.username = username;
        this.sign = sign;
        this.status = "online";
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getSign() {
        return sign;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("sign", sign)
                .add("avatar", avatar)
                .toString();
    }
}
