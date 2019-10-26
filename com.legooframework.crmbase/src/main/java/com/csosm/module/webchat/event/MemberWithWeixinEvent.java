package com.csosm.module.webchat.event;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.event.BusEvent;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.webchat.entity.WebChatUserEntity;

import java.util.List;

public class MemberWithWeixinEvent implements BusEvent {

    private final int action;
    private final StoreEntity store;
    private final List<WebChatUserEntity> webChatUsers;
    private final WebChatUserEntity webChatUser;
    private final LoginUserContext user;

    public MemberWithWeixinEvent(List<WebChatUserEntity> webChatUsers, StoreEntity store, LoginUserContext user) {
        this.action = 1;
        this.store = store;
        this.webChatUser = null;
        this.webChatUsers = webChatUsers;
        this.user = user;
    }

    @Override
    public void setLoginUser(LoginUserContext user) {

    }

    public MemberWithWeixinEvent(WebChatUserEntity webChatUser, StoreEntity store, LoginUserContext user) {
        this.action = 0;
        this.store = store;
        this.webChatUser = webChatUser;
        this.webChatUsers = null;
        this.user = user;
    }

    public StoreEntity getStore() {
        return store;
    }

    public LoginUserContext getUser() {
        return user;
    }

    public List<WebChatUserEntity> getWebChatUsers() {
        return webChatUsers;
    }

    public WebChatUserEntity getWebChatUser() {
        return webChatUser;
    }

    public boolean isBildEvent() {
        return action == 1;
    }

    public boolean isUnBildEvent() {
        return action == 0;
    }
}
