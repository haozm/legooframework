package com.csosm.commons.mvc;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.jdbc.sqlcfg.ColumnMeta;
import com.csosm.module.base.BaseModelServer;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 该类主要是是 适配 struts MVC 与 Spring MVC 之间的 会话参数传递（单向） 后期的业务逻辑开发 转入 Spring MVC 作为服务发布渠道之一
 *
 * @author Smart
 */
public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    /**
     * 返回当前登录用户信息
     */
    protected LoginUserContext loadLoginUser(HttpServletRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Assert.isInstanceOf(LoginUserContext.class, principal, "登陆账号信息异常...");
        return (LoginUserContext) principal;
    }

    /**
     * 发生异常后的统一处理机制
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Map<String, Object> defaultErrorHandler(HttpServletRequest req, Exception e) {
        logger.error(e.getMessage(), e);
        String message = e.getMessage();
        Map<String, Object> response = Maps.newHashMap();
        response.put("code", "9998");
        if (e instanceof SQLException) {
            response.put("msg", "系统发生未知错误，请联系系统管理员。");
            response.put("detail", e.getMessage());
        } else if (e instanceof IllegalStateException) {
            response.put("msg", message);
            response.put("detail", e.getMessage());
        } else if (e instanceof RuntimeException) {
            if (Strings.isNullOrEmpty(message)) {
                response.put("msg", "系统发生未知错误，请联系系统管理员。");
                response.put("detail", e.getMessage());
            } else {
                if (StringUtils.containsAny(message, "org.springframewrok.jdbc")) {
                    response.put("msg", "系统发生SQL错误，请联系系统管理员。");
                    response.put("detail", e.getMessage());
                } else {
                    response.put("msg", message);
                    response.put("detail", e.getMessage());
                }
            }
        } else {
            response.put("msg", "系统发生未知错误，请联系系统管理员。");
            response.put("detail", e.getMessage());
        }
        return response;
    }

    protected Map<String, Object> wrapperResponse(Object payload) {
        Map<String, Object> response = Maps.newHashMap();
        response.put("code", "0000");
        response.put("msg", "");
        response.put("data", payload);
        return response;
    }

    protected Map<String, Object> wrapperResponse(List<ColumnMeta> metas, Object payload) {
        Map<String, Object> response = Maps.newHashMap();
        response.put("code", "0000");
        response.put("msg", "");
        if (CollectionUtils.isNotEmpty(metas)) {
            Map<String, Object> header = Maps.newHashMap();
            header.put("metas", metas);
            response.putAll(header);
        }
        response.put("data", payload);
        return response;
    }

    protected Map<String, Object> wrapperResponse(Map<String, Object> header, Object payload) {
        Map<String, Object> response = Maps.newHashMap();
        response.put("code", "0000");
        response.put("msg", "");
        if (MapUtils.isNotEmpty(header)) response.putAll(header);
        response.put("data", payload);
        return response;
    }

    protected Map<String, Object> wrapperEmptyResponse() {
        Map<String, Object> response = Maps.newHashMap();
        response.put("code", "0000");
        response.put("msg", "");
        response.put("data", null);
        return response;
    }

    protected Map<String, Object> wrapperErrorResponse(Exception e, String message) {
        Map<String, Object> response = Maps.newHashMap();
        response.put("code", "9999");
        response.put("msg", Strings.isNullOrEmpty(message) ? e.getMessage() : message);
        response.put("detail", e.getMessage());
        return response;
    }

    protected TransactionStatus startTx(String txName) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        String tx_name = Strings.isNullOrEmpty(txName) ? UUID.randomUUID().toString() : txName;
        def.setName(tx_name);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionManager.getTransaction(def);
    }

    protected void commitTx(TransactionStatus status) {
        Preconditions.checkNotNull(status);
        transactionManager.commit(status);
    }

    protected void rollbackTx(TransactionStatus status) {
        Preconditions.checkNotNull(status);
        transactionManager.rollback(status);
    }

    protected <T> T getBean(String beanName, Class<T> clazz, HttpServletRequest request) {
        ApplicationContext app = RequestContextUtils.findWebApplicationContext(request);
        return app.getBean(beanName, clazz);
    }

    protected <T> T getBean(Class<T> clazz, HttpServletRequest request) {
        ApplicationContext app = RequestContextUtils.findWebApplicationContext(request);
        return app.getBean(clazz);
    }

    protected AsyncEventBus getAsyncEventBus(HttpServletRequest request) {
        return getBean("csosmAsyncEventBus", AsyncEventBus.class, request);
    }

    protected EventBus getEventBus(HttpServletRequest request) {
        return getBean("csosmEventbus", EventBus.class, request);
    }

    @Resource(name = "csosmTxManager")
    private DataSourceTransactionManager transactionManager;

    @Resource
    private BaseModelServer baseAdapterServer;
}
