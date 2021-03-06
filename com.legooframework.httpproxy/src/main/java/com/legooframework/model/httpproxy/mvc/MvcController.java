package com.legooframework.model.httpproxy.mvc;

import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller(value = "httpPeoxyController")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @GetMapping(value = "/health.json")
    @ResponseBody
    public JsonMessage health(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("health(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("httpPorxyBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

}
