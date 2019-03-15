package com.csosm.module.query;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.query.entity.PagingResult;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
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
import java.util.concurrent.TimeUnit;

@Controller(value = "statisticsMvcController")
@RequestMapping(value = "/statistics")
public class ReportMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ReportMvcController.class);

    @RequestMapping(value = "/{modelName}/{stmtId}/pages.json")
    @ResponseBody
    public Map<String, Object> query4Page(@PathVariable String modelName, @PathVariable String stmtId,
                                          @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[REQ][%s][%s.%s]->%s", modelName, stmtId, request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 0);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 20);
        requestBody.remove("pageNum");
        requestBody.remove("pageSize");
        Stopwatch stopwatch = Stopwatch.createStarted();
        PagingResult pages = queryEngineService.query4ReportPages(modelName, stmtId, requestBody, pageNum, pageSize, loginUser);
        if (logger.isDebugEnabled())
            logger.debug(String.format("[RSP][%s][%s.%s]->%s kill time %s", modelName, stmtId, request.getRequestURI(),
                    requestBody, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        return wrapperResponse(pages.toMap());
    }

    @RequestMapping(value = "/{modelName}/{stmtId}/list.json")
    @ResponseBody
    @SuppressWarnings("unstable")
    public Map<String, Object> query4ReportList(@PathVariable String modelName, @PathVariable String stmtId,
                                                @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[REQ][%s][%s.%s]->%s", modelName, stmtId, request.getRequestURI(), requestBody));
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<List<Map<String, Object>>> optional = queryEngineService.query4ReportList(modelName, stmtId,
                requestBody, loadLoginUser(request));
        if (logger.isDebugEnabled())
            logger.debug(String.format("[RSP][%s][%s.%s]->%s kill time %s", modelName, stmtId, request.getRequestURI(),
                    requestBody, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        return wrapperResponse(optional.isPresent() ? optional.get() : null);
    }


    @Resource
    private QueryEngineService queryEngineService;

}
