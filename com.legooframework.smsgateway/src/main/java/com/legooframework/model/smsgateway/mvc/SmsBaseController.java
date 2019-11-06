package com.legooframework.model.smsgateway.mvc;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.covariant.entity.*;
import org.apache.commons.collections4.MapUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

abstract class SmsBaseController extends BaseController {

    UserAuthorEntity loadLoginUser(Map<String, Object> requestBody, HttpServletRequest request) {
        Integer userId = MapUtils.getInteger(requestBody, "userId", 0);
        Preconditions.checkArgument(userId != 0, "登陆用户userId值非法...");
        return getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
    }

    OrgEntity loadCompanyById(int companyId, HttpServletRequest request) {
        return getBean(OrgEntityAction.class, request).loadComById(companyId);
    }

    StoEntity loadStoreById(int storeId, HttpServletRequest request) {
        return getBean(StoEntityAction.class, request).loadById(storeId);
    }
}
