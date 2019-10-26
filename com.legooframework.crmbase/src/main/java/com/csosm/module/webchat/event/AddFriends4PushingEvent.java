package com.csosm.module.webchat.event;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.BusEvent;
import com.csosm.module.webchat.entity.WechatAddFriendPushListEntity;
import com.google.common.base.MoreObjects;

import java.util.List;

public class AddFriends4PushingEvent implements BusEvent {

    private final List<WechatAddFriendPushListEntity> pushLists;

    @Override
    public void setLoginUser(LoginUserContext user) {
    }

    public AddFriends4PushingEvent(List<WechatAddFriendPushListEntity> pushLists) {
        this.pushLists = pushLists;
    }

    public List<WechatAddFriendPushListEntity> getPushLists() {
        return pushLists;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pushLists's size is ", pushLists.size())
                .toString();
    }
}
