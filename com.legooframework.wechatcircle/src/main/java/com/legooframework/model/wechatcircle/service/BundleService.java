package com.legooframework.model.wechatcircle.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.osgi.Bundle;
import org.springframework.integration.core.MessagingTemplate;

import javax.servlet.http.HttpServletRequest;

public abstract class BundleService extends BaseService {

    @Override
    protected Bundle getLocalBundle() {
        return getBean("wechatCircleBundle", Bundle.class);
    }

    MessagingTemplate getMessagingTemplate() {
        Preconditions.checkState(appCtx.containsBean("wechatCircleMessagingTemplate"),
                "未定义 wechatCircleMessagingTemplate 对应的Bean");
        return getBean("wechatCircleMessagingTemplate", MessagingTemplate.class);
    }

    JdbcQuerySupport getJdbcQuerySupport() {
        return getBean("wechatCircleJdbcQuerySupport", JdbcQuerySupport.class);
    }

    WechatCircleCommonsService getCommonsService() {
        return getBean(WechatCircleCommonsService.class);
    }

}
