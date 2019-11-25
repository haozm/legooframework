package com.legooframework.model.monitor.mvc;

import com.google.common.collect.Maps;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller(value = "monitorController")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("monitorBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    @RequestMapping(value = "/{modelId}/{sqlId}/list.json")
    @ResponseBody
    public JsonMessage query4List(@PathVariable(value = "modelId") String modelId,
                                  @PathVariable(value = "sqlId") String sqlId,
                                  @RequestBody(required = false) Map<String, Object> requestBody,
                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("query4List(url=%s,requestBody=%s)", request.getRequestURI(), requestBody));
        Map<String, Object> params = Maps.newHashMap();
        params.put("sql", String.format("%s.%s", modelId, sqlId));
        if (MapUtils.isNotEmpty(requestBody)) params.putAll(requestBody);
        Optional<List<Map<String, Object>>> results = querySupport.queryForList(modelId, sqlId, params);
        return JsonMessageBuilder.OK().withPayload(results).toMessage();
    }

    @Resource(name = "monitorJdbcQuerySupport")
    private JdbcQuerySupport querySupport;
}
