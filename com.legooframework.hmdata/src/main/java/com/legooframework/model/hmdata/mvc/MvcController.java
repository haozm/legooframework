package com.legooframework.model.hmdata.mvc;

import com.google.common.collect.Maps;
import com.legooframework.model.hmdata.service.HmDataGateWayService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController(value = "hmdataController")
public class MvcController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @PostMapping(value = "/{action}/action.json")
    public Map<String, String> hmdataGateWay(@PathVariable String action,
                                             @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("hmdataGateWay(url=%s)requestBody=%s", request.getRequestURI(), requestBody));
        if (StringUtils.equals("bankCardSign", action)) {
            return gateWayService.bankCardSign(requestBody);
        } else if (StringUtils.equals("bankCardVerify", action)) {
            return gateWayService.bankCardVerify(requestBody);
        } else if (StringUtils.equals("quickPayment", action)) {
            return gateWayService.quickPayment(requestBody);
        } else if (StringUtils.equals("payQueryOrder", action)) {
            return gateWayService.payQueryOrder(requestBody);
        } else if (StringUtils.equals("bankCardRelieve", action)) {
            return gateWayService.bankCardRelieve(requestBody);
        } else if (StringUtils.equals("drawApply", action)) {
            return gateWayService.drawApply(requestBody);
        } else {
            Map<String, String> map = Maps.newHashMap();
            map.put("resp_code", "999999");
            map.put("resp_msg", String.format("非法的请求地址%s", request.getRequestURI()));
            return map;
        }
    }

    @ExceptionHandler(value = Exception.class)
    public Map<String, String> defaultErrorHandler(HttpServletRequest req, Exception e) {
        logger.error(e.getMessage(), e);
        Map<String, String> map = Maps.newHashMap();
        map.put("resp_code", "999999");
        map.put("resp_msg", "未知异常，请联系管理员处理...");
        return map;
    }

    @Autowired
    private HmDataGateWayService gateWayService;

}
