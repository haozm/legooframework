package com.legooframework.model.jwtservice.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.jwtservice.entity.JWToken;
import com.legooframework.model.jwtservice.entity.JWTokenEntity;
import com.legooframework.model.jwtservice.entity.JWTokenEntityAction;

import java.util.List;
import java.util.Optional;

public class LoginTokenService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("jwtserviceBundle", Bundle.class);
    }

    public String applyToken(String loginName, String host, int channel) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginName), "登陆信息不可以为空值....");
        Preconditions.checkArgument(channel == 1 || channel == 2, "非法的渠道 1:WEB 2:移动端....");
        Optional<List<JWTokenEntity>> tokens = getBean(JWTokenEntityAction.class).loadEnabledTokenByLoginName(loginName, channel);
        tokens.ifPresent(ts -> getBean(JWTokenEntityAction.class).batchLogout(ts));
        return getBean(JWTokenEntityAction.class).insert(loginName, host, channel);
    }

    public Optional<JWToken> touchToken(String loginToken) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginToken), "登陆信息不可以为空值....");
        return getBean(JWTokenEntityAction.class).touched(loginToken);
    }

    public void loginOut(String loginToken) {
        getBean(JWTokenEntityAction.class).logout(loginToken);
    }

}
