package com.legooframework.model.upload.mvc;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Map;

/**
 * 该类主要是是 适配 struts MVC 与 Spring MVC 之间的 会话参数传递（单向） 后期的业务逻辑开发 转入 Spring MVC 作为服务发布渠道之一
 *
 * @author Smart
 */
public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);


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

    protected <T> T getBean(String beanName, Class<T> clazz, HttpServletRequest request) {
        ApplicationContext app = RequestContextUtils.findWebApplicationContext(request);
        return app.getBean(beanName, clazz);
    }

    protected <T> T getBean(Class<T> clazz, HttpServletRequest request) {
        ApplicationContext app = RequestContextUtils.findWebApplicationContext(request);
        return app.getBean(clazz);
    }

}
