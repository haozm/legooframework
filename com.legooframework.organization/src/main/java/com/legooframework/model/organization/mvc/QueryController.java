package com.legooframework.model.organization.mvc;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.imchat.dto.IMUserDto;
import com.legooframework.model.wechat.entity.WechatFriendEntity;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController(value = "orgQueryController")
@RequestMapping(value = "/org/q")
public class QueryController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    private IMUserDto buildIMUser(WechatFriendEntity friend) {
        return new IMUserDto(friend.getAccount().getIconUrl(), friend.getAccount().getUserName(),
                friend.getAccount().getNickName(), friend.getAccount().getConRemark());
    }

    @RequestMapping(value = "/{modelName}/{stmtId}/list.json")
    public JsonMessage query4list(@PathVariable String modelName, @PathVariable String stmtId,
                                  @RequestBody(required = false) Map<String, String> requestBody,
                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        LoginContext loginContext = getLoginContext();
        Map<String, Object> params = loginContext.toParams();
        if (MapUtils.isNotEmpty(requestBody)) {
            params.put("has_dynamic_params", true);
            params.putAll(requestBody);
        }
        Optional<List<Map<String, Object>>> list = getQuerySupport(request).queryForList(modelName, stmtId, params);
        return JsonMessageBuilder.OK().withPayload(list.isPresent() ? list.get() : new String[0]).toMessage();
    }


    @RequestMapping(value = "/{modelName}/{stmtId}/pages.json")
    public JsonMessage query4page(@PathVariable String modelName, @PathVariable String stmtId,
                                  @RequestBody Map<String, String> requestBody,
                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 0);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 0);
        requestBody.remove("pageNum");
        requestBody.remove("pageSize");
        LoginContext loginContext = getLoginContext();
        Map<String, Object> params = loginContext.toParams();
        if (MapUtils.isNotEmpty(requestBody)) {
            params.put("has_dynamic_params", true);
            params.putAll(requestBody);
        }
        PagingResult paging = getQuerySupport(request).queryForPage(modelName, stmtId, pageNum, pageSize, params);
        return JsonMessageBuilder.OK().withPayload(paging.toData()).toMessage();
    }

    private JdbcQuerySupport getQuerySupport(HttpServletRequest request) {
        return getBean("orgJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

}
