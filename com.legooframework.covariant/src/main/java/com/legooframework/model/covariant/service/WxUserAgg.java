package com.legooframework.model.covariant.service;

import com.google.common.base.MoreObjects;
import com.legooframework.model.covariant.entity.*;

import java.util.Map;
import java.util.Optional;

public class WxUserAgg implements ToReplace {

    private final WxUserEntity wxUser;
    private final StoEntity store;
    private final MemberEntity member;
    private final EmpEntity shoppingGuide;

    WxUserAgg(WxUserEntity wxUser, StoEntity store, MemberEntity member, EmpEntity shoppingGuide) {
        this.wxUser = wxUser;
        this.store = store;
        this.member = member;
        this.shoppingGuide = shoppingGuide;
    }

    public WxUserEntity getWxUser() {
        return wxUser;
    }

    public StoEntity getStore() {
        return store;
    }

    public Optional<MemberEntity> getMember() {
        return Optional.ofNullable(member);
    }

    public Optional<EmpEntity> getShoppingGuide() {
        return Optional.ofNullable(shoppingGuide);
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = wxUser.toReplaceMap();
        params.putAll(store.toReplaceMap());
        if (member != null) params.putAll(member.toReplaceMap());
        if (shoppingGuide != null) params.putAll(shoppingGuide.toReplaceMap());
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("member", member.getId())
                .add("store", store.getId())
                .add("shoppingGuide", shoppingGuide == null ? null : shoppingGuide.getId())
                .add("wxUser", wxUser == null ? null : wxUser.getId())
                .toString();
    }
}
