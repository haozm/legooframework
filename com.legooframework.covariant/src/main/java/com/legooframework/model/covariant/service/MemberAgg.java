package com.legooframework.model.covariant.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.legooframework.model.covariant.entity.*;

import java.util.Map;
import java.util.Optional;

public class MemberAgg implements ToReplace {

    private final MemberEntity member;
    private final StoEntity store;
    private final EmpEntity shoppingGuide;
    private final WxUserEntity wxUser;
    private final EWeiShopMemberEntity eweiShopUser;

    MemberAgg(MemberEntity member, StoEntity store, EmpEntity shoppingGuide, WxUserEntity wxUser,
              EWeiShopMemberEntity eweiShopUser) {
        this.member = member;
        this.store = store;
        this.shoppingGuide = shoppingGuide;
        this.wxUser = wxUser;
        this.eweiShopUser = eweiShopUser;
    }

    public Optional<EWeiShopMemberEntity> getEweiShopUser() {
        return Optional.ofNullable(eweiShopUser);
    }

    public MemberEntity getMember() {
        return member;
    }

    public Optional<WxUserEntity> getWxUser() {
        return Optional.ofNullable(wxUser);
    }

    public StoEntity getStore() {
        return store;
    }

    boolean hasShoppingGuide() {
        return this.shoppingGuide != null;
    }

    boolean hasWxUser() {
        return this.wxUser != null;
    }

    public Optional<EmpEntity> getShoppingGuide() {
        return Optional.ofNullable(shoppingGuide);
    }

    @Override
    public Map<String, Object> toReplaceMap() {
        Map<String, Object> params = member.toReplaceMap();
        params.putAll(store.toReplaceMap());
        if (hasShoppingGuide()) params.putAll(shoppingGuide.toReplaceMap());
        if (hasWxUser()) params.putAll(wxUser.toReplaceMap());
        if (getEweiShopUser().isPresent()) params.putAll(eweiShopUser.toReplaceMap());
        return params;
    }

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = this.getMember().toViewMap();
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("member", member.getId())
                .add("store", store.getId())
                .add("shoppingGuide", shoppingGuide == null ? null : shoppingGuide.getId())
                .add("wxUser", wxUser == null ? null : wxUser.getId())
                .add("eweiShopUser", eweiShopUser == null ? null : eweiShopUser.getOpenId())
                .toString();
    }
}
