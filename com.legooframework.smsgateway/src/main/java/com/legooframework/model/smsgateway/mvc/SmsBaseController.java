package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import org.apache.commons.collections4.MapUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

class SmsBaseController extends BaseController {

    UserAuthorEntity loadLoginUser(Map<String, Object> requestBody, HttpServletRequest request) {
        Integer userId = MapUtils.getInteger(requestBody, "userId", 0);
        Preconditions.checkArgument(userId != 0, "登陆用户userId值非法...");
        return getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
    }
}
