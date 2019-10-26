package com.legooframework.model.jwtservice.mvc;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.jwtservice.entity.JWToken;
import com.legooframework.model.jwtservice.entity.JWTokenEntityAction;
import com.legooframework.model.jwtservice.service.LoginTokenService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/token")
public class LoginTokenController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoginTokenController.class);

    @RequestMapping(value = "/{loginToken}/checked.json")
    public JsonMessage checkToken(@PathVariable String loginToken, HttpServletRequest request) {
        LoginContextHolder.setAnonymousCtx();
        if (logger.isDebugEnabled()) logger.debug(String.format("checkToken(%s)", loginToken));
        Optional<JWToken> token = getBean(LoginTokenService.class, request).touchToken(loginToken);
        if (token.isPresent()) {
            Map<String, Object> param = token.get().toMap();
            param.put("lastVisitTime", LocalDateTime.now().toString("yyyyMMddHHmmss"));
            return JsonMessageBuilder.OK().withPayload(param).toMessage();
        }
        return JsonMessageBuilder.ERROR("9998", "Token 无效或者过期...").toMessage();
    }

    @RequestMapping(value = "/{channel}/apply.json")
    public JsonMessage applyToken(@PathVariable String channel, @RequestBody Map<String, Object> requestBody,
                                  HttpServletRequest request) {
        LoginContextHolder.setAnonymousCtx();
        String loginName = MapUtils.getString(requestBody, "loginName");
        String loginHost = MapUtils.getString(requestBody, "loginHost");
        int _cl = StringUtils.equals("web", channel) ? 1 : 2;
        String token = getBean(LoginTokenService.class, request).applyToken(loginName, loginHost, _cl);
        if (logger.isDebugEnabled())
            logger.debug(String.format("applyToken(%s,%s) retrurn %s ", loginName, loginHost, token));
        return JsonMessageBuilder.OK().withPayload(token).toMessage();
    }

    @RequestMapping(value = "/{loginToken}/logout.json")
    public JsonMessage logout(@PathVariable String loginToken, HttpServletRequest request) {
        LoginContextHolder.setAnonymousCtx();
        getBean(JWTokenEntityAction.class, request).logout(loginToken);
        return JsonMessageBuilder.OK().toMessage();
    }

}

